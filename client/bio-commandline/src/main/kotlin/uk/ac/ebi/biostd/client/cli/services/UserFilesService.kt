package uk.ac.ebi.biostd.client.cli.services

import com.github.ajalt.clikt.output.TermUi.echo
import uk.ac.ebi.biostd.client.cli.common.getFiles
import uk.ac.ebi.biostd.client.cli.dto.DeleteUserFilesRequest
import uk.ac.ebi.biostd.client.cli.dto.UploadUserFilesRequest
import java.io.File

internal class UserFilesService {
    suspend fun uploadUserFiles(request: UploadUserFilesRequest) {
        val webClient = bioWebClient(request.securityConfig)

        suspend fun uploadFile(file: File) =
            performRequest {
                echo("Uploading file: ${file.name}")
                webClient.uploadFile(file, request.relPath)
            }

        suspend fun uploadDirectory(dir: File) = getFiles(dir).forEach { uploadFile(it) }

        when {
            request.file.isDirectory -> uploadDirectory(request.file)
            else -> uploadFile(request.file)
        }
    }

    suspend fun deleteUserFiles(request: DeleteUserFilesRequest) =
        performRequest {
            bioWebClient(request.securityConfig).deleteFile(request.fileName, request.relPath)
        }
}
