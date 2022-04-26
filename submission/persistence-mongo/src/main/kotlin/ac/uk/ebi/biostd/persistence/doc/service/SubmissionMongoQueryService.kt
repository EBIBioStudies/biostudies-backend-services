package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.getByAccNo
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.replaceFileList
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.io.use
import mu.KotlinLogging
import org.springframework.data.domain.Page
import uk.ac.ebi.extended.serialization.service.ExtFilesResolver
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.max

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
internal class SubmissionMongoQueryService(
    private val submissionRepo: SubmissionDocDataRepository,
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val fileListDocFileRepository: FileListDocFileRepository,
    private val serializationService: ExtSerializationService,
    private val toExtSubmissionMapper: ToExtSubmissionMapper,
    private val resolver: ExtFilesResolver
) : SubmissionQueryService {
    override fun existByAccNo(accNo: String): Boolean = submissionRepo.existsByAccNo(accNo)

    override fun findExtByAccNo(accNo: String, includeFileListFiles: Boolean): ExtSubmission? {
        val findByAccNo = submissionRepo.findByAccNo(accNo)
        return findByAccNo?.let { toExtSubmissionMapper.toExtSubmission(it, includeFileListFiles) }
    }

    override fun findLatestExtByAccNo(accNo: String, includeFileListFiles: Boolean): ExtSubmission? {
        val findByAccNo = submissionRepo.findLatestByAccNo(accNo)
        return findByAccNo?.let { toExtSubmissionMapper.toExtSubmission(it, includeFileListFiles) }
    }

    override fun getExtByAccNo(accNo: String, includeFileListFiles: Boolean): ExtSubmission {
        val submission = submissionRepo.getByAccNo(accNo)
        return toExtSubmissionMapper.toExtSubmission(submission, includeFileListFiles)
    }

    override fun getExtByAccNoAndVersion(accNo: String, version: Int, includeFileListFiles: Boolean): ExtSubmission {
        val document = submissionRepo.getByAccNoAndVersion(accNo, version)
        return toExtSubmissionMapper.toExtSubmission(document, includeFileListFiles)
    }

    override fun expireSubmissions(accNumbers: List<String>) {
        submissionRepo.expireVersions(accNumbers)
    }

    override fun getExtendedSubmissions(filter: SubmissionFilter): Page<ExtSubmission> {
        return submissionRepo.getSubmissionsPage(filter).map { toExtSubmissionMapper.toExtSubmission(it, false) }
    }

    override fun getSubmissionsByUser(owner: String, filter: SubmissionFilter): List<BasicSubmission> {
        val (requestsCount, requests) = requestRepository.findActiveRequest(filter, owner)
        val offset = max(0, filter.offset - requestsCount)
        val limit = filter.limit - requests.size
        val submissionFilter = filter.copy(limit = limit, offset = offset)
        val drafts = requests.map { it.asBasicSubmission() }
        val draftsKeys = drafts.associateBy { it.accNo }
        val submissions = getSubmissions(owner, submissionFilter).filter { draftsKeys.containsKey(it.accNo).not() }

        return drafts + submissions
    }

    private fun getSubmissions(owner: String, filter: SubmissionFilter): List<BasicSubmission> =
        when (filter.limit) {
            0 -> emptyList()
            else -> submissionRepo.getSubmissions(filter, owner).map { it.asBasicSubmission() }
        }

    override fun getPendingRequest(accNo: String, version: Int): SubmissionRequest {
        val request = requestRepository.getByAccNoAndVersionAndStatus(accNo, version, REQUESTED)
        val stored = serializationService.deserialize(request.submission.toString())
        val full = stored.copy(section = stored.section.replaceFileList { calculate(stored, it) })
        return SubmissionRequest(full, request.fileMode, request.draftKey)
    }

    private fun calculate(sub: ExtSubmission, fileList: ExtFileList): ExtFileList {
        val newFileList = resolver.createEmptyFile(sub.accNo, sub.version, fileList.fileName)
        return fileList.copy(file = copyFile(sub.accNo, sub.submitter, fileList.file, newFileList))
    }

    private fun copyFile(accNo: String, submitter: String, source: File, target: File): File {
        use(source.inputStream(), target.outputStream()) { input, output -> copy(accNo, submitter, input, output) }
        return target
    }

    private fun copy(accNo: String, submitter: String, input: InputStream, output: OutputStream) {
        val files = serializationService.deserializeList(input)
            .onEachIndexed { idx, file ->
                logger.info { "accNo=$accNo, submitter=$submitter mapping file ${file.filePath}, ${idx + 1}" }
            }
            .map { extFile -> loadFileAttributes(extFile) }
        serializationService.serialize(files, output)
    }

    private fun loadFileAttributes(file: ExtFile): ExtFile = when (file) {
        is FireDirectory -> file
        is FireFile -> file
        is NfsFile -> file.copy(md5 = file.file.md5(), size = file.file.size())
    }

    override fun getReferencedFiles(accNo: String, fileListName: String): List<ExtFile> =
        fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(accNo, 0, fileListName)
            .map { it.file.toExtFile() }

    private fun DocSubmissionRequest.asBasicSubmission() =
        serializationService.deserialize(submission.toString()).asBasicSubmission()
}
