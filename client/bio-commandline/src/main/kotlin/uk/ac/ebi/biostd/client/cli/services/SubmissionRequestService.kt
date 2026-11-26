package uk.ac.ebi.biostd.client.cli.services

import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.model.RequestStatus

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
