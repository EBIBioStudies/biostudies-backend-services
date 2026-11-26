package ac.uk.ebi.biostd.client.integration.web

import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.biostd.api.security.LoginRequest
import uk.ac.ebi.biostd.api.security.RegisterRequest
import uk.ac.ebi.biostd.api.security.UserProfile
import uk.ac.ebi.biostd.commons.http.ext.RequestParams
import uk.ac.ebi.biostd.commons.http.ext.post
import uk.ac.ebi.biostd.commons.http.ext.postForObject

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

    override fun login(loginRequest: LoginRequest): UserProfile =
        client.postForObject<UserProfile>("/auth/login", RequestParams(body = loginRequest))

    override fun registerUser(registerRequest: RegisterRequest) {
        client.post("/auth/register", RequestParams(body = registerRequest))
    }

    companion object {
        fun create(baseUrl: String) = SecurityWebClient(baseUrl, webClientBuilder(baseUrl).build())
    }
}
