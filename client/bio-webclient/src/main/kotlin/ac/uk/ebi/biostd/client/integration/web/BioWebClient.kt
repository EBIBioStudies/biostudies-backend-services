package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.api.SubmissionClientImpl
import ac.uk.ebi.biostd.client.interceptor.OnBehalfInterceptor
import ac.uk.ebi.biostd.client.interceptor.TokenInterceptor
import ac.uk.ebi.biostd.integration.SerializationConfig
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class BioWebClient internal constructor(
    private val submissionClient: SubmissionClient
) : SubmissionClient by submissionClient {
    companion object {
        fun create(baseUrl: String, token: String): BioWebClient = BioWebClient(
            SubmissionClientImpl(
                createRestTemplate(baseUrl, token),
                SerializationConfig.serializationService(),
                ExtSerializationService()
            )
        )

        fun create(
            baseUrl: String,
            token: String,
            onBehalf: String
        ): BioWebClient = BioWebClient(
            SubmissionClientImpl(
                createRestTemplate(baseUrl, token, onBehalf),
                SerializationConfig.serializationService(),
                ExtSerializationService()
            )
        )

        private fun createRestTemplate(baseUrl: String, token: String) = template(baseUrl).apply {
            interceptors.add(TokenInterceptor(token))
        }

        private fun createRestTemplate(baseUrl: String, token: String, onBehalf: String) =
            createRestTemplate(baseUrl, token).apply {
                interceptors.add(OnBehalfInterceptor(onBehalf))
            }
    }
}
