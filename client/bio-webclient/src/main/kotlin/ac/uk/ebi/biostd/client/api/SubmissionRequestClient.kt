package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.SubmissionRequestOperations
import ebi.ac.uk.commons.http.ext.getForObject
import ebi.ac.uk.model.RequestStatus
import org.springframework.web.reactive.function.client.WebClient

private const val SUBMISSION_REQUEST_URL = "/submissions/requests"

class SubmissionRequestClient(
    private val client: WebClient,
) : SubmissionRequestOperations {
    override fun getSubmissionRequestStatus(
        accNo: String,
        version: Int,
    ): RequestStatus {
        return client.getForObject("$SUBMISSION_REQUEST_URL/$accNo/$version/status")
    }
}
