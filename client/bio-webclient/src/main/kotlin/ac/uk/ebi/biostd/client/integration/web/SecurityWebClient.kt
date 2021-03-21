package ac.uk.ebi.biostd.client.integration.web

import ebi.ac.uk.api.security.CheckUserRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.UserProfile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

class SecurityWebClient private constructor(
    private val baseUrl: String,
    private val restTemplate: RestTemplate
) : SecurityOperations {

    override fun getAuthenticatedClient(user: String, password: String, onBehalf: String?): BioWebClient =
        when (onBehalf) {
            null -> BioWebClient.create(baseUrl, login(LoginRequest(user, password)).sessid)
            else -> BioWebClient.create(baseUrl, login(LoginRequest(user, password)).sessid, onBehalf)
        }

    override fun login(loginRequest: LoginRequest): UserProfile =
        restTemplate.postForObject("/auth/login", createHttpEntity(loginRequest))

    override fun registerUser(registerRequest: RegisterRequest) {
        restTemplate.postForLocation("/auth/register", createHttpEntity(registerRequest))
    }

    override fun checkUser(checkUserRequest: CheckUserRequest) {
        restTemplate.postForLocation("/auth/check-user", createHttpEntity(checkUserRequest))
    }

    private fun <T> createHttpEntity(body: T): HttpEntity<T> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        return HttpEntity(body, headers)
    }

    companion object {
        fun create(baseUrl: String) = SecurityWebClient(baseUrl, template(baseUrl))
    }
}
