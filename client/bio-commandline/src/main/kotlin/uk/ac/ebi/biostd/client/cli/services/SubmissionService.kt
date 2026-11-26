package uk.ac.ebi.biostd.client.cli.services

import com.github.ajalt.clikt.output.TermUi.echo
import uk.ac.ebi.biostd.base.isNotBlank
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.integration.web.BioWebClient
import uk.ac.ebi.biostd.coroutines.FOREVER
import uk.ac.ebi.biostd.coroutines.waitUntil
import uk.ac.ebi.biostd.extended.model.StorageMode
import uk.ac.ebi.biostd.model.RequestStatus
import uk.ac.ebi.biostd.model.RequestStatus.INVALID
import uk.ac.ebi.biostd.model.RequestStatus.POST_PROCESSED
import uk.ac.ebi.biostd.model.RequestStatus.PROCESSED
import uk.ac.ebi.biostd.model.RequestStatus.REQUESTED
import uk.ac.ebi.biostd.model.SubmissionTransferOptions
import kotlin.time.Duration.Companion.seconds

@Suppress("TooManyFunctions")
internal class SubmissionService {
    suspend fun submit(request: SubmissionRequest): Unit =
        performRequest {
            val client = bioWebClient(request.securityConfig)
            val (accNo, version) =
                client.submitMultipart(
                    request.submissionFile,
                    request.parameters,
                    request.files,
                )
            echo("SUCCESS: Submission $accNo, version: $version is in queue to be processed")

            if (request.await) client.waitForSubmission(accNo, version)
        }

    fun migrate(
        securityConfig: SecurityConfig,
        accNo: String,
        target: StorageMode,
    ) = performRequest {
        bioWebClient(securityConfig).migrateSubmission(accNo, target)
    }

    suspend fun delete(
        securityConfig: SecurityConfig,
        accNoList: List<String>,
    ) = performRequest {
        bioWebClient(securityConfig).deleteSubmissions(accNoList)
    }

    suspend fun transferEmailUpdate(
        securityConfig: SecurityConfig,
        options: SubmissionTransferOptions,
    ) = performRequest {
        bioWebClient(securityConfig).transferEmailUpdate(options)
    }

    suspend fun transfer(
        securityConfig: SecurityConfig,
        options: SubmissionTransferOptions,
    ) = performRequest {
        val to = options.newOwner
        val client = bioWebClient(securityConfig)

        runCatching {
            client.getExtUser(options.newOwner)
        }.getOrElse {
            require(options.userName.isNotBlank()) {
                "New owner '$to' doesn't exist. Use option '-un' or '--userName' to provide the full name."
            }
        }

        client.transferSubmissions(options)
    }

    suspend fun generateDoi(
        securityConfig: SecurityConfig,
        accNo: String,
    ) = performRequest {
        bioWebClient(securityConfig).generateDoi(accNo)
    }

    companion object {
        private const val CHECK_INTERVAL = 20L
    }

    private suspend fun BioWebClient.waitForSubmission(
        accNo: String,
        version: Int,
    ) {
        var status: RequestStatus = REQUESTED
        waitUntil(
            checkInterval = CHECK_INTERVAL.seconds,
            timeout = FOREVER,
        ) {
            status = getSubmissionRequestStatus(accNo, version)

            val completed = status == PROCESSED || status == POST_PROCESSED || status == INVALID
            if (completed.not()) echo("INFO: Waiting for submission to be processed. Current status: $status")

            return@waitUntil completed
        }

        if (status == PROCESSED || status == POST_PROCESSED) {
            echo("SUCCESS: Submission $accNo, version: $version is processed")
        } else {
            val errors = getSubmissionRequestErrors(accNo, version)
            echo("ERROR: Submission $accNo, version: $version failed with errors:\n ${errors.joinToString("\n")}")
        }
    }
}
