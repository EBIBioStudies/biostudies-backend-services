package uk.ac.ebi.biostd.client.cli.services

import uk.ac.ebi.biostd.client.cli.dto.DeleteUserFilesRequest
import uk.ac.ebi.biostd.client.cli.dto.UploadUserFilesRequest

internal class UserFilesService {
    suspend fun uploadUserFiles(request: UploadUserFilesRequest) =
        performRequest {
            bioWebClient(request.securityConfig).uploadFiles(request.files, request.relPath)
        }

    suspend fun deleteUserFiles(request: DeleteUserFilesRequest) =
        performRequest {
            bioWebClient(request.securityConfig).deleteFile(request.fileName, request.relPath)
        }
}
