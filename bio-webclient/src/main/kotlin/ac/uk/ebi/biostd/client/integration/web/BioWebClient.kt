package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.client.submission.SubmissionClientImpl
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory

class BioWebClient private constructor(private val submissionClient: SubmissionClient) :
    SubmissionClient by submissionClient {

    companion object {

        fun create(baseUrl: String, token: String): BioWebClient {
            return BioWebClient(SubmissionClientImpl(SerializationService(), createRestTemplate(baseUrl, token)))
        }

        private fun createRestTemplate(baseUrl: String, token: String) = RestTemplate().apply {
            uriTemplateHandler = DefaultUriBuilderFactory(baseUrl)
            interceptors = listOf(TokenInterceptor(token))
        }
    }
}
