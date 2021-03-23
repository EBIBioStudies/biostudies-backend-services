package ac.uk.ebi.biostd.client.interceptor

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class HttpHeaderInterceptor(
    private val headerName: String,
    private val headerValue: String
) : ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        if (request.headers[headerName].isNullOrEmpty()) request.headers.set(headerName, headerValue)
        return execution.execute(request, body)
    }
}
