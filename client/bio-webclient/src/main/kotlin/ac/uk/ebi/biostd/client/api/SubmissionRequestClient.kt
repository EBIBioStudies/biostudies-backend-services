package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.SubmissionRequestOperations
import ebi.ac.uk.model.RequestStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

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

    override suspend fun archiveSubmissionRequest(
        accNo: String,
        version: Int,
    ) {
        client
            .post()
            .uri("$SUBMISSION_REQUEST_URL/$accNo/$version/archive")
            .retrieve()
            .awaitBodilessEntity()
    }
}
