package ac.uk.ebi.biostd.client.submission

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.SubmissionClient
import ebi.ac.uk.model.Submission
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.File

private const val SUBMISSIONS_URL = "/submissions"
private const val FILES_URL = "/files"
private const val USER_FILES_URL = "$FILES_URL/user"

internal class SubmissionClientImpl(
    private val serializationService: SerializationService,
    private val template: RestTemplate
) : SubmissionClient {

    override fun uploadFile(files: List<File>) {
        val headers = HttpHeaders().apply { contentType = MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply { files.forEach { add("files", FileSystemResource(it)) } }
        template.postForEntity(USER_FILES_URL, HttpEntity(body, headers), Void::class.java)
    }

    override fun submitSingle(submission: Submission, format: SubmissionFormat) =
        submitSingle(httpEntity(getBody(submission, format)) { contentType = format.mediaType; accept = listOf(format.mediaType) }, format)

    override fun submitSingle(submission: String, format: SubmissionFormat) =
        submitSingle(httpEntity(submission) { contentType = format.mediaType; accept = listOf(format.mediaType) }, format)

    private fun submitSingle(request: HttpEntity<String>, format: SubmissionFormat) =
        template.exchange(SUBMISSIONS_URL, POST, request, String::class.java)
            .map { body -> serializationService.deserializeSubmission(body, format.asSubFormat()) }

    private fun httpEntity(body: String, function: HttpHeaders.() -> Unit) = HttpEntity(body, HttpHeaders().apply(function))

    private fun getBody(submission: Submission, format: SubmissionFormat) = when (format) {
        SubmissionFormat.JSON -> serializationService.serializeSubmission(submission, SubFormat.JSON)
        SubmissionFormat.TSV -> serializationService.serializeSubmission(submission, SubFormat.TSV)
        SubmissionFormat.XML -> serializationService.serializeSubmission(submission, SubFormat.XML)
    }
}
