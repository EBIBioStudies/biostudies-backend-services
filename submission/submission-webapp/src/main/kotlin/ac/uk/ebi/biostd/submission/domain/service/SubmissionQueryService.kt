package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import java.io.File
import java.nio.file.Files

class SubmissionQueryService(
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
    private val filesRepository: SubmissionFilesPersistenceService,
    private val serializationService: SerializationService,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val toFileListMapper: ToFileListMapper,
) {
    fun getSubmission(accNo: String, subFormat: SubFormat): String {
        val submission = submissionPersistenceQueryService.getExtByAccNo(accNo)
        return serializationService.serializeSubmission(
            toSubmissionMapper.toSimpleSubmission(submission),
            subFormat
        )
    }

    fun getFileList(accNo: String, fileListName: String, format: SubFormat): File {
        val submission = submissionPersistenceQueryService.getExtByAccNo(accNo)
        val fileList = filesRepository.getReferencedFiles(submission, fileListName)
        val targetFile = Files.createTempFile(accNo, fileListName)
        return toFileListMapper.serialize(fileList, format, targetFile.toFile())
    }

    fun getSubmissions(
        filter: SubmissionFilter,
    ): List<BasicSubmission> = submissionPersistenceQueryService.getSubmissionsByUser(filter)
}
