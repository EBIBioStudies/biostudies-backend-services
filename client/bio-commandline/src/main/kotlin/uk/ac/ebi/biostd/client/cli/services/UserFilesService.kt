package uk.ac.ebi.biostd.client.cli.services

import uk.ac.ebi.biostd.client.cli.dto.UserFilesRequest

internal class UserFilesService {
    suspend fun uploadUserFiles(request: UserFilesRequest) =
        performRequest {
            bioWebClient(request.securityConfig).uploadFiles(request.files, request.relPath)
        }
}
