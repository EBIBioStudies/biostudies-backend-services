package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.exception.InvalidHostErrorHandler
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.UserProfile
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import org.springframework.web.util.DefaultUriBuilderFactory

class SecurityWebClient private constructor(
    private val baseUrl: String,
    private val restTemplate: RestTemplate
) : SecurityOperations {

    override fun getAuthenticatedClient(user: String, password: String): BioWebClient =
        BioWebClient.create(baseUrl, login(LoginRequest(user, password)).sessid)

    override fun login(loginRequest: LoginRequest): UserProfile =
        restTemplate.postForObject("/auth/login", loginRequest)!!

    override fun registerUser(registerRequest: RegisterRequest) {
        restTemplate.postForLocation("/auth/register", registerRequest)
    }

    companion object {

        fun create(baseUrl: String) = SecurityWebClient(
            baseUrl,
            RestTemplate().apply {
                errorHandler = InvalidHostErrorHandler()
                uriTemplateHandler = DefaultUriBuilderFactory(baseUrl)
            })
    }
}
