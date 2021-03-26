package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import com.github.ajalt.clikt.core.PrintMessage
import ebi.ac.uk.model.Submission
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.File

data class SubmissionRequest(
    val server: String,
    val user: String,
    val password: String,
    val onBehalf: String?,
    val file: File,
    val attached: List<File>
)

data class SubmissionDeleteRequest(
    val server: String,
    val user: String,
    val password: String,
    val onBehalf: String?,
    val accNo: String
)

class SubmissionService {
    fun submit(request: SubmissionRequest): Submission {
        return runCatching { submitRequest(request) }.getOrElse { throw PrintMessage(ExceptionUtils.getMessage(it)) }
    }

    fun delete(request: SubmissionDeleteRequest) {
        return runCatching { deleteRequest(request) }.getOrElse { throw PrintMessage(ExceptionUtils.getMessage(it)) }
    }

    private fun deleteRequest(request: SubmissionDeleteRequest) {
        val client = SecurityWebClient.create(request.server).getAuthenticatedClient(request.user, request.password)
        return client.deleteSubmission(request.accNo)
    }

    private fun submitRequest(request: SubmissionRequest): Submission {
        val client = SecurityWebClient.create(request.server).getAuthenticatedClient(request.user, request.password)
        return client.submitSingle(request.file, request.attached).body
    }
}
