package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.dto.AcceptedSubmission
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import com.github.ajalt.clikt.output.TermUi.echo
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.model.RequestStatus.PROCESSED
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.dto.TransferRequest
import uk.ac.ebi.biostd.client.cli.dto.ValidateFileListRequest
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds

@Suppress("TooManyFunctions")
internal class SubmissionService {
    suspend fun submit(request: SubmissionRequest): AcceptedSubmission =
        performRequest {
            val client = bioWebClient(request.securityConfig)
            val acceptedSubmission = client.asyncSubmitSingle(request.submissionFile, request.filesConfig)
            val (accNo, version) = acceptedSubmission

            echo("SUCCESS: Submission $accNo, version: $version is in queue to be processed")

            if (request.timeout > 0) client.waitForSubmission(request.timeout.toLong(), accNo, version)

            return acceptedSubmission
        }

    private suspend fun BioWebClient.waitForSubmission(
        timeout: Long,
        accNo: String,
        version: Int,
    ) {
        waitUntil(
            duration = ofMinutes(timeout),
            interval = ofSeconds(CHECK_INTERVAL),
        ) {
            val status = getSubmissionRequestStatus(accNo, version)
            val isProcessed = status == PROCESSED

            if (isProcessed.not()) echo("INFO: Waiting for submission to be processed")

            isProcessed
        }

        echo("SUCCESS: Submission $accNo, version: $version is processed")
    }

    fun transfer(request: TransferRequest) =
        performRequest {
            val client = bioWebClient(request.securityConfig)
            client.transferSubmission(request.accNo, request.target)
        }

    fun delete(request: DeletionRequest) =
        performRequest {
            val client = bioWebClient(request.securityConfig)
            client.deleteSubmissions(request.accNoList)
        }

    fun migrate(request: MigrationRequest): Unit =
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

    fun validateFileList(request: ValidateFileListRequest) =
        performRequest {
            val (fileListPath, accNo, rootPath) = request
            val client = bioWebClient(request.securityConfig)
            client.validateFileList(fileListPath, rootPath, accNo)
        }

    companion object {
        private const val CHECK_INTERVAL = 20L
    }
}
