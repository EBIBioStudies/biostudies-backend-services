package ac.uk.ebi.biostd.client.integration.web

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

const val HEADER_NAME = "X-Session-Token"

class TokenInterceptor(private val token: String) : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        request.headers.set(HEADER_NAME, token)
        return execution.execute(request, body)
    }
}