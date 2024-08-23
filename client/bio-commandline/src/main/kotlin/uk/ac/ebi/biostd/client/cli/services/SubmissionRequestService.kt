package uk.ac.ebi.biostd.client.cli.services

import ebi.ac.uk.model.RequestStatus
import uk.ac.ebi.biostd.client.cli.dto.SubmissionStatusRequest

internal class SubmissionRequestService {
    suspend fun getRequestStatus(request: SubmissionStatusRequest): RequestStatus =
        performRequest {
            val client = bioWebClient(request.securityConfig)
            client.getSubmissionRequestStatus(request.accNo, request.version)
        }
}
