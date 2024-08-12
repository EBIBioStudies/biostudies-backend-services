package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import com.github.ajalt.clikt.output.TermUi.echo
import ebi.ac.uk.coroutines.FOREVER
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.model.RequestStatus.PROCESSED
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.dto.TransferRequest
import uk.ac.ebi.biostd.client.cli.dto.ValidateFileListRequest
import java.time.Duration.ofSeconds

@Suppress("TooManyFunctions")
internal class SubmissionService {
    suspend fun submit(request: SubmissionRequest): Unit =
        performRequest {
            val client = bioWebClient(request.securityConfig)
            val (accNo, version) = client.asyncSubmitSingle(request.submissionFile, request.parameters)
            echo("SUCCESS: Submission $accNo, version: $version is in queue to be processed")

            if (request.await) client.waitForSubmission(accNo, version)
        }

    suspend fun transfer(request: TransferRequest) =
        performRequest {
            val client = bioWebClient(request.securityConfig)
            client.transferSubmission(request.accNo, request.target)
        }

    suspend fun delete(request: DeletionRequest) =
        performRequest {
            val client = bioWebClient(request.securityConfig)
            client.deleteSubmissions(request.accNoList)
        }

    suspend fun migrate(request: MigrationRequest): Unit =
        performRequest {
            val sourceClient = bioWebClient(request.sourceSecurityConfig)
            val targetClient = bioWebClient(request.targetSecurityConfig)
            val source = sourceClient.getExtByAccNo(request.accNo, true)

            val submission = if (request.targetOwner != null) source.copy(owner = request.targetOwner) else source
            when (request.async) {
                true -> targetClient.submitExtAsync(submission)
                false -> targetClient.submitExt(submission)
            }
        }

    suspend fun validateFileList(request: ValidateFileListRequest) =
        performRequest {
            val (fileListPath, accNo, rootPath) = request
            val client = bioWebClient(request.securityConfig)
            client.validateFileList(fileListPath, rootPath, accNo)
        }

    companion object {
        private const val CHECK_INTERVAL = 20L
    }

    private suspend fun BioWebClient.waitForSubmission(
        accNo: String,
        version: Int,
    ) {
        waitUntil(
            checkInterval = ofSeconds(CHECK_INTERVAL),
            timeout = FOREVER,
        ) {
            val status = getSubmissionRequestStatus(accNo, version)
            echo("INFO: Waiting for submission to be PROCESSED. Current status: $status")
            return@waitUntil status == PROCESSED
        }

        echo("SUCCESS: Submission $accNo, version: $version is processed")
    }
}
