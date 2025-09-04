package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.api.SubmitClientImpl
import ac.uk.ebi.biostd.common.SerializationConfig
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.UriComponentsBuilder
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig

private const val ON_BEHALF_PARAM = "onBehalf"
private const val AUTH_HEADER = "X-SESSION-TOKEN"

class BioWebClient internal constructor(
    private val submissionClient: SubmitClient,
) : SubmitClient by submissionClient {
    companion object {
        fun create(
            baseUrl: String,
            token: String,
        ): BioWebClient =
            BioWebClient(
                SubmitClientImpl(
                    createWebClient(baseUrl, token),
                    SerializationConfig.serializationService(),
                    ExtSerializationConfig.extSerializationService(),
                ),
            )

        fun create(
            baseUrl: String,
            token: String,
            onBehalf: String,
        ): BioWebClient =
            BioWebClient(
                SubmitClientImpl(
                    createWebClient(baseUrl, token, onBehalf),
                    SerializationConfig.serializationService(),
                    ExtSerializationConfig.extSerializationService(),
                ),
            )

        private fun createWebClient(
            baseUrl: String,
            token: String,
            onBehalf: String? = null,
        ): WebClient {
            val uriBuilder = UriComponentsBuilder.fromUriString(baseUrl)
            onBehalf?.let { uriBuilder.queryParam(ON_BEHALF_PARAM, onBehalf) }

            return webClientBuilder(baseUrl)
                .uriBuilderFactory(DefaultUriBuilderFactory(uriBuilder))
                .defaultHeader(AUTH_HEADER, token)
                .build()
        }
    }
}
