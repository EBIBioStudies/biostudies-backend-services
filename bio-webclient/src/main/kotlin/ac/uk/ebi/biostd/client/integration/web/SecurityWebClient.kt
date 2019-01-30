package ac.uk.ebi.biostd.client.integration.web

import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.LoginResponse
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import org.springframework.web.util.DefaultUriBuilderFactory

class SecurityWebClient private constructor(
        private val baseUrl: String,
        private val restTemplate: RestTemplate) : SecurityOperations {

    override fun auhtenticate(user: String, password: String): BioWebClient {
        return BioWebClient.create(baseUrl, login(user, password).sessid)
    }

    override fun login(user: String, password: String): LoginResponse {
        return restTemplate.postForObject("/auth/login", LoginRequest(user, password))!!
    }

    companion object {

        fun create(baseUrl: String): SecurityWebClient {
            return SecurityWebClient(baseUrl, RestTemplate().apply { uriTemplateHandler = DefaultUriBuilderFactory(baseUrl) })
        }
    }
}
