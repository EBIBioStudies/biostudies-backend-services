package uk.ac.ebi.biostd.client.cli.services

import ebi.ac.uk.model.RequestStatus
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig

internal class SubmissionRequestService {
    suspend fun getRequestStatus(
        securityConfig: SecurityConfig,
        accNo: String,
        version: Int,
    ): RequestStatus =
        performRequest {
            val client = bioWebClient(securityConfig)
            client.getSubmissionRequestStatus(accNo, version)
        }
}
