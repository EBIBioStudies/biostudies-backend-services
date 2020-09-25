package uk.ac.ebi.fire.client.integration.web

import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import uk.ac.ebi.fire.client.api.FireClient
import uk.ac.ebi.fire.client.exception.FireWebClientErrorHandler

class FireWebClient private constructor(
    private val fireClient: FireClient
): FireOperations by fireClient {
    companion object {
        fun create(
            tmpDirPath: String,
            fireHost: String,
            username: String,
            password: String
        ): FireWebClient = FireWebClient(FireClient(tmpDirPath, createRestTemplate(fireHost, username, password)))

        private fun createRestTemplate(fireHost: String, username: String, password: String) =
            RestTemplate().apply {
                uriTemplateHandler = DefaultUriBuilderFactory(fireHost)
                interceptors.add(BasicAuthenticationInterceptor(username, password))
                errorHandler = FireWebClientErrorHandler()
            }
    }
}
