package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.MultipartAsyncSubmissionOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.FILE_MODE
import ebi.ac.uk.model.constants.PREFERRED_SOURCE
import ebi.ac.uk.model.constants.SUBMISSION
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.io.File

private const val SUBMIT_URL = "/submissions/async"

class MultiPartAsyncSubmissionClient(
    private val template: RestTemplate,
    private val serializationService: SerializationService
) : MultipartAsyncSubmissionOperations {
    override fun asyncSubmitSingle(
        submission: File,
        files: List<File>,
        attrs: Map<String, String>,
        fileMode: FileMode,
        preferredSource: PreferredSource
    ) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val multiPartBody = getMultipartBody(files, fileMode, preferredSource, FileSystemResource(submission))
        attrs.entries.forEach { multiPartBody.add(it.key, it.value) }
        template.postForEntity<String>("$SUBMIT_URL/direct", (HttpEntity(multiPartBody, headers)))
    }

    override fun asyncSubmitSingle(
        submission: String,
        format: SubmissionFormat,
        files: List<File>,
        fileMode: FileMode,
        preferredSource: PreferredSource
    ) {
        val headers = createHeaders(format)
        val body = getMultipartBody(files, fileMode, preferredSource, submission)
        template.postForEntity<String>(SUBMIT_URL, HttpEntity(body, headers))
    }

    override fun asyncSubmitSingle(
        submission: Submission,
        format: SubmissionFormat,
        files: List<File>,
        fileMode: FileMode,
        preferredSource: PreferredSource
    ) {
        val headers = createHeaders(format)
        val serializedSubmission = serializationService.serializeSubmission(submission, format.asSubFormat())
        val body = getMultipartBody(files, fileMode, preferredSource, serializedSubmission)
        template.postForEntity<String>(SUBMIT_URL, HttpEntity(body, headers))
    }

    private fun createHeaders(format: SubmissionFormat) = HttpHeaders().apply {
        contentType = MediaType.MULTIPART_FORM_DATA
        accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
        setSubmissionType(format.submissionType)
    }

    private fun getMultipartBody(
        files: List<File>,
        fileMode: FileMode,
        preferredSource: PreferredSource,
        submission: Any
    ) = LinkedMultiValueMap(
        files.map { FILES to FileSystemResource(it) }
            .plus(SUBMISSION to submission)
            .plus(FILE_MODE to fileMode.name)
            .plus(PREFERRED_SOURCE to preferredSource.name)
            .groupBy({ it.first }, { it.second })
    )
}
