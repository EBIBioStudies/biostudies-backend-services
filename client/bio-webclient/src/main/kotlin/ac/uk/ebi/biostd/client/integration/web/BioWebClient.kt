package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.api.SubmissionClientImpl
import ac.uk.ebi.biostd.client.exception.BioWebClientErrorHandler
import ac.uk.ebi.biostd.client.interceptor.OnBehalfInterceptor
import ac.uk.ebi.biostd.client.interceptor.TokenInterceptor
import ac.uk.ebi.biostd.integration.SerializationConfig
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory

class BioWebClient private constructor(
    private val submissionClient: SubmissionClient
) : SubmissionClient by submissionClient {
    companion object {
        fun create(baseUrl: String, token: String): BioWebClient = BioWebClient(
            SubmissionClientImpl(SerializationConfig.serializationService(), createRestTemplate(baseUrl, token)))

        fun create(baseUrl: String, token: String, onBehalf: String): BioWebClient = BioWebClient(SubmissionClientImpl(
            SerializationConfig.serializationService(), createRestTemplate(baseUrl, token, onBehalf)))

        private fun createRestTemplate(baseUrl: String, token: String) = RestTemplate().apply {
            uriTemplateHandler = DefaultUriBuilderFactory(baseUrl)
            interceptors.add(TokenInterceptor(token))
            errorHandler = BioWebClientErrorHandler()
        }

        private fun createRestTemplate(baseUrl: String, token: String, onBehalf: String) =
            createRestTemplate(baseUrl, token).apply {
                interceptors.add(OnBehalfInterceptor(onBehalf))
            }
    }
}
