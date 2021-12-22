package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.FileListNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.allDocSections
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.util.collections.firstOrElse
import org.springframework.data.domain.Page
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File

@Suppress("TooManyFunctions")
internal class SubmissionMongoQueryService(
    private val submissionRepo: SubmissionDocDataRepository,
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val fileListDocFileRepository: FileListDocFileRepository,
    private val serializationService: ExtSerializationService,
    private val toExtSubmissionMapper: ToExtSubmissionMapper
) : SubmissionQueryService {
    override fun existByAccNo(accNo: String): Boolean = submissionRepo.existsByAccNo(accNo)

    override fun findExtByAccNo(accNo: String): ExtSubmission? =
        submissionRepo.findByAccNo(accNo)?.let { toExtSubmissionMapper.toExtSubmission(it) }

    override fun getExtByAccNo(accNo: String): ExtSubmission {
        val submission = loadSubmission(accNo)
        return toExtSubmissionMapper.toExtSubmission(submission)
    }

    override fun getExtByAccNoAndVersion(accNo: String, version: Int): ExtSubmission {
        val document = submissionRepo.getByAccNoAndVersion(accNo, version)
        return toExtSubmissionMapper.toExtSubmission(document)
    }

    override fun expireSubmissions(accNumbers: List<String>) {
        submissionRepo.expireVersions(accNumbers)
    }

    override fun getExtendedSubmissions(filter: SubmissionFilter): Page<Result<ExtSubmission>> {
        return submissionRepo.getSubmissionsPage(filter)
            .map { runCatching { toExtSubmissionMapper.toExtSubmission(it) } }
    }

    override fun getSubmissionsByUser(email: String, filter: SubmissionFilter): List<BasicSubmission> {
        val requests = requestRepository.findActiveRequest(filter, email).map { it.asBasicSubmission() }
        return requests + getSubmissions(filter.limit - requests.size, email, filter)
    }

    override fun getRequest(accNo: String, version: Int): SubmissionRequest {
        val request = requestRepository.getByAccNoAndVersion(accNo, version)
        val fileLists = request.fileList.associate { it.fileName to File(it.filePath) }
        val submission = serializationService.deserialize<ExtSubmission>(request.submission.toString())
        val fullSubmission = submission.copy(section = processSection(submission.section) { loadFiles(it, fileLists) })
        return SubmissionRequest(
            submission = fullSubmission,
            fileMode = request.fileMode,
            draftKey = request.draftKey
        )
    }

    private fun loadFiles(fileList: ExtFileList, files: Map<String, File>): ExtFileList {
        val fileListFile = files.getValue(fileList.fileName)
        val filesTable = serializationService.deserialize<ExtFileTable>(fileListFile.readText())
        return fileList.copy(files = filesTable.files)
    }

    private fun processSection(section: ExtSection, processFile: (file: ExtFileList) -> ExtFileList): ExtSection {
        return section.copy(
            fileList = section.fileList?.let { processFile(it) },
            sections = TODO()
        )
    }

    override fun getReferencedFiles(accNo: String, fileListName: String): List<ExtFile> =
        loadSubmission(accNo)
            .allDocSections
            .mapNotNull { it.fileList }
            .filter { it.fileName == fileListName }
            .firstOrElse { throw FileListNotFoundException(accNo, fileListName) }
            .let { fileList -> fileListDocFileRepository.findAllById(fileList.files.map { it.fileId }) }
            .map { it.file.toExtFile() }

    private fun loadSubmission(accNo: String) =
        submissionRepo.findByAccNo(accNo) ?: throw SubmissionNotFoundException(accNo)

    private fun DocSubmissionRequest.asBasicSubmission() =
        serializationService.deserialize<ExtSubmission>(submission.toString()).asBasicSubmission()

    private fun getSubmissions(limit: Int, email: String, filter: SubmissionFilter): List<BasicSubmission> =
        when (limit) {
            0 -> emptyList()
            else -> submissionRepo.getSubmissions(filter.copy(limit = limit), email).map { it.asBasicSubmission() }
        }
}
