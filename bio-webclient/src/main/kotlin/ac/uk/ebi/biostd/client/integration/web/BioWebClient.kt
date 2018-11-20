package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.submission.SubmissionClientImpl
import ebi.ac.uk.model.Submission
import org.springframework.http.HttpRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory

class BioWebClient private constructor(baseUrl: String, private val submissionClient: SubmissionClient) :
    SubmissionClient by submissionClient {

    companion object {

        fun create(baseUrl: String, token: String): BioWebClient {
            return BioWebClient(baseUrl, SubmissionClientImpl(SerializationService(), createRestTemplate(baseUrl, token)))
        }

        private fun createRestTemplate(baseUrl: String, token: String): RestTemplate {
            return RestTemplate().apply {
                uriTemplateHandler = DefaultUriBuilderFactory(baseUrl)
                interceptors = listOf(TokenInterceptor(token))
            }
        }
    }
}

const val HEADER_NAME = "X-Session-Token"

class TokenInterceptor(private val token: String) : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        request.headers.set(HEADER_NAME, token)
        return execution.execute(request, body)
    }
}

interface SubmissionClient {

    fun submitSingle(submission: Submission, format: SubmissionFormat = SubmissionFormat.JSON): ResponseEntity<Submission>

    fun submitSingle(submission: String, format: SubmissionFormat = SubmissionFormat.JSON): ResponseEntity<Submission>
}
