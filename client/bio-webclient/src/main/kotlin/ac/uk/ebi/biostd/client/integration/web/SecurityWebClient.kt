package ac.uk.ebi.biostd.client.integration.web

import ebi.ac.uk.api.security.CheckUserRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.UserProfile
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

class SecurityWebClient private constructor(
    private val baseUrl: String,
    private val restTemplate: RestTemplate
) : SecurityOperations {
    override fun getAuthenticatedClient(
        user: String,
        password: String,
        onBehalf: String?,
        enableTsvFeature: Boolean
    ): BioWebClient {
        val sessId = login(LoginRequest(user, password)).sessid

        return when (onBehalf) {
            null -> BioWebClient.create(baseUrl, sessId, enableTsvExtFeature = enableTsvFeature)
            else -> BioWebClient.create(baseUrl, sessId, onBehalf, enableTsvFeature)
        }
    }

    override fun login(loginRequest: LoginRequest): UserProfile =
        restTemplate.postForObject("/auth/login", jsonHttpEntityOf(loginRequest))

    override fun registerUser(registerRequest: RegisterRequest) {
        restTemplate.postForLocation("/auth/register", jsonHttpEntityOf(registerRequest))
    }

    override fun checkUser(checkUserRequest: CheckUserRequest) {
        restTemplate.postForLocation("/auth/check-user", jsonHttpEntityOf(checkUserRequest))
    }

    companion object {
        fun create(baseUrl: String) = SecurityWebClient(baseUrl, template(baseUrl))
    }
}
