package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.api.SubmissionClientImpl
import ac.uk.ebi.biostd.client.interceptor.OnBehalfInterceptor
import ac.uk.ebi.biostd.client.interceptor.TokenInterceptor
import ac.uk.ebi.biostd.common.SerializationConfig
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig

class BioWebClient internal constructor(
    private val submissionClient: SubmissionClient
) : SubmissionClient by submissionClient {
    companion object {
        fun create(
            baseUrl: String,
            token: String,
            enableTsvExtFeature: Boolean = false
        ): BioWebClient = BioWebClient(
            SubmissionClientImpl(
                createRestTemplate(baseUrl, token),
                SerializationConfig.serializationService(enableTsvExtFeature),
                ExtSerializationConfig.extSerializationService()
            )
        )

        fun create(
            baseUrl: String,
            token: String,
            onBehalf: String,
            enableTsvExtFeature: Boolean = false
        ): BioWebClient = BioWebClient(
            SubmissionClientImpl(
                createRestTemplate(baseUrl, token, onBehalf),
                SerializationConfig.serializationService(enableTsvExtFeature),
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
