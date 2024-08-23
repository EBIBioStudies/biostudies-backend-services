package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.SubmissionRequestOperations
import ebi.ac.uk.model.RequestStatus
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.web.reactive.function.client.WebClient
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

    override suspend fun archiveSubmissionRequest(
        accNo: String,
        version: Int,
    ) {
        client
            .post()
            .uri("$SUBMISSION_REQUEST_URL/$accNo/$version/archive")
            .retrieve()
            .bodyToMono(Void::class.java)
            .awaitSingle()
    }
}
