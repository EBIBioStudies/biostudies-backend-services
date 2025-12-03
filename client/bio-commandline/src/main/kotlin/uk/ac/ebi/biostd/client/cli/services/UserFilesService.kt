package uk.ac.ebi.biostd.client.cli.services

import com.github.ajalt.clikt.output.TermUi.echo
import uk.ac.ebi.biostd.client.cli.common.getFiles
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import java.io.File

internal class UserFilesService {
    suspend fun uploadUserFiles(
        securityConfig: SecurityConfig,
        file: File,
        relPath: String,
    ) {
        val webClient = bioWebClient(securityConfig)

        suspend fun uploadFile(file: File) =
            performRequest {
                echo("Uploading file: ${file.name}")
                webClient.uploadFile(file, relPath)
            }

        suspend fun uploadDirectory(dir: File) = getFiles(dir).forEach { uploadFile(it) }

        when {
            file.isDirectory -> uploadDirectory(file)
            else -> uploadFile(file)
        }
    }

    suspend fun deleteUserFiles(
        securityConfig: SecurityConfig,
        fileName: String,
        relPath: String,
    ) = performRequest {
        bioWebClient(securityConfig).deleteFile(fileName, relPath)
    }
}
