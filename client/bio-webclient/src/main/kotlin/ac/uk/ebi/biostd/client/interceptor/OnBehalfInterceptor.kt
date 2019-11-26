package ac.uk.ebi.biostd.client.interceptor

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.lang.Nullable
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

private const val PARAMETER_NAME = "onBehalf"

class OnBehalfInterceptor(private val onBehalf: String) : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val uri = UriComponentsBuilder.fromUri(request.uri).queryParam(PARAMETER_NAME, onBehalf).build().toUri()
        return execution.execute(RequestWrapper(request, uri), body)
    }

    class RequestWrapper(private val request: HttpRequest, private val uri: URI) : HttpRequest {
        @Nullable
        override fun getMethod(): HttpMethod? = this.request.method

        override fun getMethodValue(): String = this.request.methodValue
        override fun getURI(): URI = uri
        override fun getHeaders(): HttpHeaders = this.request.headers
    }
}
