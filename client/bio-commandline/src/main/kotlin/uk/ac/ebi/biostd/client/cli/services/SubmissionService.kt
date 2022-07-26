package uk.ac.ebi.biostd.client.cli.services

import ebi.ac.uk.model.Submission
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.dto.ValidateFileListRequest

@Suppress("TooManyFunctions")
internal class SubmissionService {
    fun submit(request: SubmissionRequest): Submission = performRequest { submitRequest(request) }

    fun submitAsync(request: SubmissionRequest) = performRequest { submitAsyncRequest(request) }

    fun delete(request: DeletionRequest) = performRequest { deleteRequest(request) }

    fun migrate(request: MigrationRequest) = performRequest { migrateRequest(request) }

    fun validateFileList(request: ValidateFileListRequest) = performRequest { validateFileListRequest(request) }

    private fun submitRequest(request: SubmissionRequest): Submission {
        val (server, user, password, onBehalf) = request.securityConfig

        return bioWebClient(server, user, password, onBehalf)
            .submitSingle(request.submissionFile, request.filesConfig)
            .body
    }

    private fun submitAsyncRequest(request: SubmissionRequest) {
        val (server, user, password, onBehalf) = request.securityConfig

        bioWebClient(server, user, password, onBehalf)
            .asyncSubmitSingle(request.submissionFile, request.filesConfig)
    }

    private fun deleteRequest(request: DeletionRequest) {
        val (server, user, password) = request.securityConfig

        bioWebClient(server, user, password).deleteSubmissions(request.accNoList)
    }

    private fun migrateRequest(rqt: MigrationRequest) {
        val sourceConfig = rqt.sourceSecurityConfig
        val targetConfig = rqt.targetSecurityConfig
        val sourceClient = bioWebClient(sourceConfig.server, sourceConfig.user, sourceConfig.password)
        val targetClient = bioWebClient(targetConfig.server, targetConfig.user, targetConfig.password)
        val source = sourceClient.getExtByAccNo(rqt.accNo, true)
        val submission = if (rqt.targetOwner != null) source.copy(owner = rqt.targetOwner) else source

        when (rqt.async) {
            true -> targetClient.submitExtAsync(submission, rqt.fileMode)
            false -> targetClient.submitExt(submission, rqt.fileMode)
        }
    }

    private fun validateFileListRequest(request: ValidateFileListRequest) {
        val (fileListPath, accNo, rootPath) = request
        val (server, user, password, onBehalf) = request.securityConfig

        bioWebClient(server, user, password, onBehalf).validateFileList(fileListPath, rootPath, accNo)
    }
}
