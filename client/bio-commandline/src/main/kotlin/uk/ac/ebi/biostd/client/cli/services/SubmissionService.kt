package uk.ac.ebi.biostd.client.cli.services

import ebi.ac.uk.model.Submission
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.dto.ValidateFileListRequest

/**
 * In charge of performing submission command line operations.
 */
@Suppress("TooManyFunctions")
internal class SubmissionService {
    fun submit(request: SubmissionRequest): Submission = performRequest { submitRequest(request) }
    fun submitAsync(request: SubmissionRequest) = performRequest { submitAsyncRequest(request) }
    fun delete(request: DeletionRequest) = performRequest { deleteRequest(request) }
    fun migrate(request: MigrationRequest) = performRequest { migrateRequest(request) }
    fun validateFileList(request: ValidateFileListRequest) = performRequest { validateFileListRequest(request) }

    private fun submitRequest(request: SubmissionRequest): Submission =
        bioWebClient(request.server, request.user, request.password, request.onBehalf)
            .submitSingle(request.file, request.attached, fileMode = request.fileMode)
            .body

    private fun submitAsyncRequest(request: SubmissionRequest) =
        bioWebClient(request.server, request.user, request.password, request.onBehalf)
            .asyncSubmitSingle(request.file, request.attached, fileMode = request.fileMode)

    private fun deleteRequest(request: DeletionRequest) =
        bioWebClient(request.server, request.user, request.password).deleteSubmissions(request.accNoList)

    private fun migrateRequest(request: MigrationRequest) {
        val sourceClient = bioWebClient(request.source, request.sourceUser, request.sourcePassword)
        val targetClient = bioWebClient(request.target, request.targetUser, request.targetPassword)
        when (request.async) {
            true -> targetClient.submitExtAsync(sourceClient.getExtByAccNo(request.accNo, true), request.fileMode)
            false -> targetClient.submitExt(sourceClient.getExtByAccNo(request.accNo, true), request.fileMode)
        }
    }

    private fun validateFileListRequest(request: ValidateFileListRequest) =
        bioWebClient(request.server, request.user, request.password, request.onBehalf)
            .validateFileList(request.fileListPath)
}
