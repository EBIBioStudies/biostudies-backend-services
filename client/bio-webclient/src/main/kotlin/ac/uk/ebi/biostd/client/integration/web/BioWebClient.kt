package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.api.SubmitClientImpl
import ac.uk.ebi.biostd.client.interceptor.OnBehalfInterceptor
import ac.uk.ebi.biostd.client.interceptor.TokenInterceptor
import ac.uk.ebi.biostd.common.SerializationConfig
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig

class BioWebClient internal constructor(
    private val submissionClient: SubmitClient,
) : SubmitClient by submissionClient {
    companion object {
        fun create(
            baseUrl: String,
            token: String,
        ): BioWebClient = BioWebClient(
            SubmitClientImpl(
                createRestTemplate(baseUrl, token),
                SerializationConfig.serializationService(),
                ExtSerializationConfig.extSerializationService()
            )
        )

        fun create(
            baseUrl: String,
            token: String,
            onBehalf: String,
        ): BioWebClient = BioWebClient(
            SubmitClientImpl(
                createRestTemplate(baseUrl, token, onBehalf),
                SerializationConfig.serializationService(),
                ExtSerializationConfig.extSerializationService()
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
