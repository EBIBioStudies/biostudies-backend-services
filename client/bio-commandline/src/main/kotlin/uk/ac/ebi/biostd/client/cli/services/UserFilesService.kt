package uk.ac.ebi.biostd.client.cli.services

import com.github.ajalt.clikt.output.TermUi.echo
import ebi.ac.uk.coroutines.FOREVER
import ebi.ac.uk.coroutines.waitUntil
import uk.ac.ebi.biostd.client.cli.common.getFiles
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import java.io.File
import kotlin.time.Duration.Companion.seconds

internal class UserFilesService {
    suspend fun datamoverUpload(
        securityConfig: SecurityConfig,
        file: File,
        relPath: String,
    ) {
        val webClient = bioWebClient(securityConfig)
        val jobId = performRequest { webClient.uploadFilesThroughCluster(file, relPath) }
        echo("Started transferring user files through datamover job $jobId")

        var status: String
        waitUntil(
            checkInterval = CHECK_INTERVAL.seconds,
            timeout = FOREVER,
        ) {
            status = performRequest { webClient.getClusterJobStatus(jobId) }.trim()
            echo("Job $jobId status: $status")
            status == COMPLETED_STATUS
        }

        echo("Finished transferring user files. Datamover job $jobId completed")
    }

    suspend fun httpUpload(
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

    private companion object {
        private const val COMPLETED_STATUS = "COMPLETED"
        private const val CHECK_INTERVAL = 5L
    }
}
