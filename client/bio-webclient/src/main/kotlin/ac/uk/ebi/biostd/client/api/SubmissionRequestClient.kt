package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.SubmissionRequestOperations
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.ac.ebi.biostd.model.RequestStatus

private const val SUBMISSION_REQUEST_URL = "/submissions/requests"

class SubmissionRequestClient(
    private val client: WebClient,
) : SubmissionRequestOperations {
    override suspend fun getSubmissionRequestStatus(
        accNo: String,
        version: Int,
    ): RequestStatus =
        client
            .get()
            .uri("$SUBMISSION_REQUEST_URL/$accNo/$version/status")
            .retrieve()
            .awaitBody<RequestStatus>()

    override suspend fun getSubmissionRequestErrors(
        accNo: String,
        version: Int,
    ): List<String> =
        client
            .get()
            .uri("$SUBMISSION_REQUEST_URL/$accNo/$version/errors")
            .retrieve()
            .awaitBody<List<String>>()
}
