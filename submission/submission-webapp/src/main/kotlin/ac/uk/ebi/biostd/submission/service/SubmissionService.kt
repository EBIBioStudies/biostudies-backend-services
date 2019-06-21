package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.integration.SubmissionService
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.submission.processing.SubmissionProcessor
import ebi.ac.uk.utils.FilesSource

class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val submissionService: SubmissionService,
    private val submissionProcessor: SubmissionProcessor,
    private val serializationService: SerializationService
) {

    fun getSimpleAsJson(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission.toSimpleSubmission(), SubFormat.JSON_PRETTY)
    }

    fun getSimpleAsXml(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission.toSimpleSubmission(), SubFormat.XML)
    }

    fun getSimpleAsTsv(accNo: String): String {
        val submission = submissionRepository.getByAccNo(accNo)
        return serializationService.serializeSubmission(submission.toSimpleSubmission(), SubFormat.TSV)
    }

    fun delete(accNo: String, user: SecurityUser) {
        require(submissionService.canDelete(accNo, asUser(user)))
        submissionRepository.expireSubmission(accNo)
    }

    fun submit(submission: Submission, securityUser: SecurityUser, files: FilesSource): Submission {
        val user = asUser(securityUser)
        val extSubmission = submissionProcessor.processSubmission(submission, user, files)
        val submitted = submissionService.submit(extSubmission, user)
        return submitted.toSimpleSubmission()
    }

    private fun asUser(user: SecurityUser): User = User(user.id, user.email, user.secret)
}
