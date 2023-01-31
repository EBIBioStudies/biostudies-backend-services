package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.getByAccNo
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSING
import org.springframework.data.domain.Page
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import kotlin.math.max

@Suppress("TooManyFunctions")
internal class SubmissionMongoPersistenceQueryService(
    private val submissionRepo: SubmissionDocDataRepository,
    private val toExtSubmissionMapper: ToExtSubmissionMapper,
    private val serializationService: ExtSerializationService,
    private val fileListDocFileRepository: FileListDocFileDocDataRepository,
    private val requestRepository: SubmissionRequestDocDataRepository,
) : SubmissionPersistenceQueryService {
    override fun existByAccNo(accNo: String): Boolean {
        return submissionRepo.existsByAccNo(accNo)
    }

    override fun existByAccNoAndVersion(accNo: String, version: Int): Boolean {
        return submissionRepo.existsByAccNoAndVersion(accNo, version)
    }

    override fun findExtByAccNo(accNo: String, includeFileListFiles: Boolean): ExtSubmission? {
        val findByAccNo = submissionRepo.findByAccNo(accNo)
        return findByAccNo?.let { toExtSubmissionMapper.toExtSubmission(it, includeFileListFiles) }
    }

    override fun findLatestExtByAccNo(accNo: String, includeFileListFiles: Boolean): ExtSubmission? {
        val findByAccNo = submissionRepo.findByAccNo(accNo)
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

    override fun getExtendedSubmissions(filter: SubmissionFilter): Page<ExtSubmission> {
        return submissionRepo.getSubmissionsPage(filter).map { toExtSubmissionMapper.toExtSubmission(it, false) }
    }

    override fun getSubmissionsByUser(owner: String, filter: SubmissionFilter): List<BasicSubmission> {
        val (requestsCount, requests) = requestRepository.findActiveRequests(filter, owner)
        val submissionFilter = filter.copy(
            limit = filter.limit - requests.size,
            offset = max(0, filter.offset - requestsCount),
            notIncludeAccNo = requests.map { it.accNo }.toSet()
        )

        return requests
            .map { serializationService.deserialize(it.submission.toString()) }
            .map { it.asBasicSubmission(PROCESSING) }
            .plus(findSubmissions(owner, submissionFilter))
    }

    override fun getReferencedFiles(
        sub: ExtSubmission,
        fileListName: String,
    ): List<ExtFile> {
        return fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(sub.accNo, 0, fileListName)
            .map { it.file.toExtFile(sub.released, sub.relPath) }
    }

    override fun findReferencedFile(
        sub: ExtSubmission,
        path: String,
    ): ExtFile? {
        return fileListDocFileRepository
            .findBySubmissionAccNoAndSubmissionVersionAndFilePath(sub.accNo, sub.version, path)
            .firstOrNull()
            ?.file
            ?.toExtFile(sub.released, sub.relPath)
    }

    private fun findSubmissions(owner: String, filter: SubmissionFilter): List<BasicSubmission> =
        when (filter.limit) {
            0 -> emptyList()
            else -> submissionRepo.getSubmissions(filter, owner).map { it.asBasicSubmission(PROCESSED) }
        }
}
