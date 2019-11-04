package ac.uk.ebi.biostd.client.interceptor

import ac.uk.ebi.biostd.client.exception.WebClientException
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class ServerValidationInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse =
        runCatching { execution.execute(request, body) }
            .getOrElse { throw WebClientException(NOT_FOUND, "Connection Error: The provided server wasn't found") }
}
