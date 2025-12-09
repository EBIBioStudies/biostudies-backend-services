package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import com.github.ajalt.clikt.output.TermUi.echo
import ebi.ac.uk.coroutines.FOREVER
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.INVALID
import ebi.ac.uk.model.RequestStatus.POST_PROCESSED
import ebi.ac.uk.model.RequestStatus.PROCESSED
import ebi.ac.uk.model.RequestStatus.REQUESTED
import ebi.ac.uk.model.SubmissionTransferOptions
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import java.time.Duration.ofSeconds

@Suppress("TooManyFunctions")
internal class SubmissionService {
    suspend fun submit(request: SubmissionRequest): Unit =
        performRequest {
            val client = bioWebClient(request.securityConfig)
            val (accNo, version) = client.submitMultipartAsync(request.submissionFile, request.parameters, request.files)
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

    suspend fun transfer(
        securityConfig: SecurityConfig,
        options: SubmissionTransferOptions,
    ) = performRequest {
        bioWebClient(securityConfig).transferSubmissions(options)
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
            checkInterval = ofSeconds(CHECK_INTERVAL),
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
