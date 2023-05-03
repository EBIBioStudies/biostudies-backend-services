package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File
import java.nio.file.Files

class SubmissionQueryService(
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
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
        val fileList = submissionPersistenceQueryService.getReferencedFiles(submission, fileListName)
        val targetFile = Files.createTempFile(accNo, fileListName)
        return toFileListMapper.serialize(fileList, format, targetFile.toFile())
    }

    fun getSubmissions(
        user: SecurityUser,
        filter: SubmissionFilter,
    ): List<BasicSubmission> = submissionPersistenceQueryService.getSubmissionsByUser(user.email, filter)
}
