package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.integration.SubmissionService
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.submission.processing.SubmissionProcessor

class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val submissionService: SubmissionService,
    private val submissionProcessor: SubmissionProcessor,
    private val serializationService: SerializationService
) {

    fun getSubmissionAsJson(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission.toSimpleSubmission(), SubFormat.JSON_PRETTY)
    }

    fun getSubmissionAsXml(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission.toSimpleSubmission(), SubFormat.XML)
    }

    fun getSubmissionAsTsv(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission.toSimpleSubmission(), SubFormat.TSV)
    }

    fun deleteSubmission(accNo: String, user: SecurityUser) {
        require(submissionService.canDelete(accNo, asUser(user)))
        submissionRepository.expireSubmission(accNo)
    }

    fun submit(
        submission: Submission,
        user: SecurityUser,
        files: FilesSource
    ): Submission {
        val simple = asUser(user)
        val extSubmission = submissionProcessor.processSubmission(submission, simple, files)
        val submimited = submissionService.submit(extSubmission, simple)
        return submimited.toSimpleSubmission()
    }

    private fun asUser(user: SecurityUser): User = User(user.id, user.email, user.secret)
}
