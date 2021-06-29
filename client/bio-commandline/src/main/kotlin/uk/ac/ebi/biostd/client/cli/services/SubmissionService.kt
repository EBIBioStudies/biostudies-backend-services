package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import com.github.ajalt.clikt.core.PrintMessage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Submission
import org.apache.commons.lang3.exception.ExceptionUtils
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest

/**
 * In charge of perform submission command line operations.
 */
internal class SubmissionService {
    fun submit(request: SubmissionRequest): Submission = performRequest { submitRequest(request) }

    fun submitAsync(request: SubmissionRequest) = performRequest { submitAsyncRequest(request) }

    fun delete(request: DeletionRequest) = performRequest { deleteRequest(request) }

    fun migrate(request: MigrationRequest) = performRequest { migrateRequest(request) }

    private fun submitRequest(request: SubmissionRequest): Submission =
        bioWebClient(request.server, request.user, request.password).submitSingle(request.file, request.attached).body

    private fun submitAsyncRequest(request: SubmissionRequest) =
        bioWebClient(request.server, request.user, request.password).asyncSubmitSingle(request.file, request.attached)

    private fun deleteRequest(request: DeletionRequest) =
        bioWebClient(request.server, request.user, request.password).deleteSubmissions(request.accNoList)

    private fun migrateRequest(request: MigrationRequest) {
        val sourceClient = bioWebClient(request.source, request.sourceUser, request.sourcePassword)
        val targetClient = bioWebClient(request.target, request.targetUser, request.targetPassword)
        targetClient.submitExt(migratedSubmissions(sourceClient.getExtByAccNo(request.accNo), request.targetOwner))
    }

    private fun migratedSubmissions(submission: ExtSubmission, targetOwner: String?) =
        if (targetOwner == null) submission else submission.copy(owner = targetOwner)

    companion object {
        private inline fun <T> performRequest(request: () -> T) =
            runCatching { request() }
                .getOrElse { throw PrintMessage(ExceptionUtils.getMessage(it)) }

        private fun bioWebClient(server: String, user: String, password: String) =
            SecurityWebClient
                .create(server)
                .getAuthenticatedClient(user, password)
    }
}
