package uk.ac.ebi.fire.client.integration.web

import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpRequestInitializer

class FireAuthRequestInitializer(
    private val username: String,
    private val password: String
) : ClientHttpRequestInitializer {
    override fun initialize(request: ClientHttpRequest) {
        request.headers.setBasicAuth(username, password)
    }
}
