package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.submission.SubmissionClientImpl
import ebi.ac.uk.model.Submission
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import java.io.File

class BioWebClient private constructor(private val submissionClient: SubmissionClient) :
    SubmissionClient by submissionClient {

    companion object {

        fun create(baseUrl: String, token: String): BioWebClient {
            return BioWebClient(SubmissionClientImpl(SerializationService(), createRestTemplate(baseUrl, token)))
        }

        private fun createRestTemplate(baseUrl: String, token: String): RestTemplate {
            return RestTemplate().apply {
                uriTemplateHandler = DefaultUriBuilderFactory(baseUrl)
                interceptors = listOf(TokenInterceptor(token))
            }
        }
    }
}

interface SubmissionClient {

    fun submitSingle(submission: Submission, format: SubmissionFormat = SubmissionFormat.JSON): ResponseEntity<Submission>

    fun submitSingle(submission: String, format: SubmissionFormat = SubmissionFormat.JSON): ResponseEntity<Submission>

    fun uploadFile(files: List<File>)
}
