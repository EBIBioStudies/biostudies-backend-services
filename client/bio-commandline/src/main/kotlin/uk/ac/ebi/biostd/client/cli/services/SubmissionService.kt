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

    private fun submitRequest(request: SubmissionRequest): Submission {
        val (server, user, password, onBehalf, file, attached, fileMode, preferredSource) = request

        return bioWebClient(server, user, password, onBehalf)
            .submitSingle(file, attached, fileMode = fileMode, preferredSource = preferredSource)
            .body
    }

    private fun submitAsyncRequest(request: SubmissionRequest) {
        val (server, user, password, onBehalf, file, attached, fileMode, preferredSource) = request

        bioWebClient(server, user, password, onBehalf)
            .asyncSubmitSingle(file, attached, fileMode = fileMode, preferredSource = preferredSource)
    }

    private fun deleteRequest(request: DeletionRequest) =
        bioWebClient(request.server, request.user, request.password).deleteSubmissions(request.accNoList)

    private fun migrateRequest(rqt: MigrationRequest) {
        val sourceClient = bioWebClient(rqt.source, rqt.sourceUser, rqt.sourcePassword)
        val targetClient = bioWebClient(rqt.target, rqt.targetUser, rqt.targetPassword)
        val source = sourceClient.getExtByAccNo(rqt.accNo, true)
        val submission = if (rqt.targetOwner != null) source.copy(owner = rqt.targetOwner) else source
        when (rqt.async) {
            true -> targetClient.submitExtAsync(submission, rqt.fileMode)
            false -> targetClient.submitExt(submission, rqt.fileMode)
        }
    }

    private fun validateFileListRequest(request: ValidateFileListRequest) =
        bioWebClient(request.server, request.user, request.password, request.onBehalf)
            .validateFileList(request.fileListPath)
}
