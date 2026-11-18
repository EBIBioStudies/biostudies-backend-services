package uk.ac.ebi.biostd.asserts

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.INVALID
import ebi.ac.uk.model.RequestStatus.POST_PROCESSED
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionId
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpStatus.BAD_REQUEST
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

data class SubmissionResult(
    val body: SubmissionId,
    val status: RequestStatus,
)

fun assertThat(result: SubmissionResult): SubmissionIdAssert = SubmissionIdAssert(result)

class SubmissionIdAssert(
    actual: SubmissionResult,
) : AbstractAssert<SubmissionIdAssert, SubmissionResult>(actual, SubmissionIdAssert::class.java) {
    fun isSuccessful() {
        assertThat(actual).isNotNull
        assertThat(actual.status).isEqualTo(POST_PROCESSED)
        assertThat(actual.body.accNo).isNotBlank()
        assertThat(actual.body.version).isPositive()
    }
}

suspend fun BioWebClient.submitAndWait(
    submission: Submission,
    format: SubmissionFormat = JSON,
    submitParameters: SubmitParameters? = null,
    register: OnBehalfParameters? = null,
): SubmissionResult = awaitSubmission(submit(submission, format, submitParameters, register))

suspend fun BioWebClient.submitAndWait(
    submission: String,
    format: SubmissionFormat = JSON,
    submitParameters: SubmitParameters? = null,
    register: OnBehalfParameters? = null,
): SubmissionResult = awaitSubmission(submit(submission, format, submitParameters, register))

suspend fun BioWebClient.submitMultipartAndWait(
    submission: String,
    format: SubmissionFormat,
    parameters: SubmitParameters,
    files: List<File> = emptyList(),
): SubmissionResult = awaitSubmission(submitMultipart(submission, format, parameters, files))

suspend fun BioWebClient.submitMultipartAndWait(
    submission: Submission,
    format: SubmissionFormat,
    parameters: SubmitParameters,
    files: List<File> = emptyList(),
): SubmissionResult = awaitSubmission(submitMultipart(submission, format, parameters, files))

suspend fun BioWebClient.submitMultipartAndWait(
    submission: File,
    parameters: SubmitParameters,
    files: List<File> = emptyList(),
): SubmissionResult = awaitSubmission(submitMultipart(submission, parameters, files))

suspend fun BioWebClient.submitFromDraftAndWait(draftKey: String): SubmissionResult = awaitSubmission(submitFromDraft(draftKey))

private suspend fun BioWebClient.awaitSubmission(submissionId: SubmissionId): SubmissionResult {
    lateinit var status: RequestStatus

    waitUntil(timeout = 2.minutes, checkInterval = 100.milliseconds) {
        status = getSubmissionRequestStatus(submissionId.accNo, submissionId.version)
        status == POST_PROCESSED || status == INVALID
    }

    require(status == POST_PROCESSED) {
        val errors = getSubmissionRequestErrors(submissionId.accNo, submissionId.version)
        throw WebClientException(BAD_REQUEST, errors.joinToString("\n"))
    }

    return SubmissionResult(submissionId, status)
}
