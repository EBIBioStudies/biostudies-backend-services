package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.dto.AcceptedSubmission
import ebi.ac.uk.model.Submission
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.dto.TransferRequest
import uk.ac.ebi.biostd.client.cli.dto.ValidateFileListRequest

@Suppress("TooManyFunctions")
internal class SubmissionService {
    fun submit(rqt: SubmissionRequest): Submission =
        performRequest {
            val client = bioWebClient(rqt.securityConfig)
            return client.submitSingle(rqt.submissionFile, rqt.filesConfig).body
        }

    fun submitAsync(request: SubmissionRequest): AcceptedSubmission =
        performRequest {
            val client = bioWebClient(request.securityConfig)
            return client.asyncSubmitSingle(request.submissionFile, request.filesConfig)
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
}
