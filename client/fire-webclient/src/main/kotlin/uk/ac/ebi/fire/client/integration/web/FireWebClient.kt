package uk.ac.ebi.fire.client.integration.web

import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import uk.ac.ebi.fire.client.api.FireClient
import uk.ac.ebi.fire.client.exception.FireWebClientErrorHandler

private const val FIRE_API_BASE = "fire"

class FireWebClient private constructor(
    private val fireClient: FireClient
) : FireOperations by fireClient {
    companion object {
        fun create(
            tmpDirPath: String,
            fireHost: String,
            fireVersion: String,
            username: String,
            password: String
        ): FireWebClient =
            FireWebClient(FireClient(tmpDirPath, createRestTemplate(fireHost, fireVersion, username, password)))

        private fun createRestTemplate(fireHost: String, fireVersion: String, username: String, password: String) =
            RestTemplate().apply {
                uriTemplateHandler = DefaultUriBuilderFactory("$fireHost/$FIRE_API_BASE/$fireVersion")
                errorHandler = FireWebClientErrorHandler()
                clientHttpRequestInitializers.add(FireAuthRequestInitializer(username, password))
                requestFactory = SimpleClientHttpRequestFactory().apply {
                    setReadTimeout(0)
                    setConnectTimeout(0)
                    setBufferRequestBody(false)
                }
            }
    }
}
