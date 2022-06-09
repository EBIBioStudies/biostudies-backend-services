package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.security.integration.model.api.SecurityUser

class SubmissionQueryService(
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
    private val serializationService: SerializationService,
    private val toSubmissionMapper: ToSubmissionMapper,
) {
    fun getSubmissionAsJson(accNo: String): String {
        val submission = submissionPersistenceQueryService.getExtByAccNo(accNo)
        return serializationService.serializeSubmission(
            toSubmissionMapper.toSimpleSubmission(submission),
            SubFormat.JsonFormat.JsonPretty
        )
    }

    fun getSubmissionAsXml(accNo: String): String {
        val submission = submissionPersistenceQueryService.getExtByAccNo(accNo)
        return serializationService.serializeSubmission(
            toSubmissionMapper.toSimpleSubmission(submission),
            SubFormat.XmlFormat
        )
    }

    fun getSubmissionAsTsv(accNo: String): String {
        val submission = submissionPersistenceQueryService.getExtByAccNo(accNo)
        return serializationService.serializeSubmission(
            toSubmissionMapper.toSimpleSubmission(submission),
            SubFormat.TsvFormat.Tsv
        )
    }

    fun getSubmissions(
        user: SecurityUser,
        filter: SubmissionFilter
    ): List<BasicSubmission> = submissionPersistenceQueryService.getSubmissionsByUser(user.email, filter)
}
