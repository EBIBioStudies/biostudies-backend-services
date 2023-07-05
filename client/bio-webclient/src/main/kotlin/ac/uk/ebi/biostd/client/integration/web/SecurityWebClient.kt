package ac.uk.ebi.biostd.client.integration.web

import ebi.ac.uk.api.security.CheckUserRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.UserProfile
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.post
import ebi.ac.uk.commons.http.ext.postForObject
import org.springframework.web.reactive.function.client.WebClient

class SecurityWebClient private constructor(
    private val baseUrl: String,
    private val client: WebClient,
) : SecurityOperations {
    override fun getAuthenticatedClient(
        user: String,
        password: String,
        onBehalf: String?,
    ): BioWebClient {
        val sessId = login(LoginRequest(user, password)).sessid

        return when (onBehalf) {
            null -> BioWebClient.create(baseUrl, sessId)
            else -> BioWebClient.create(baseUrl, sessId, onBehalf)
        }
    }

    override fun login(loginRequest: LoginRequest): UserProfile {
        return client.postForObject<UserProfile>("/auth/login", RequestParams(body = loginRequest))
    }

    override fun registerUser(registerRequest: RegisterRequest) {
        client.post("/auth/register", RequestParams(body = registerRequest))
    }

    override fun checkUser(checkUserRequest: CheckUserRequest) {
        client.post("/auth/check-registration", RequestParams(body = checkUserRequest))
    }

    companion object {
        fun create(baseUrl: String) = SecurityWebClient(baseUrl, webClientBuilder(baseUrl).build())
    }
}
