package ac.uk.ebi.biostd.client.exception

import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

class SecurityWebClientErrorHandler : ResponseErrorHandler {
    override fun hasError(response: ClientHttpResponse) = response.statusCode.isError

    override fun handleError(response: ClientHttpResponse) =
        throw SecurityWebClientException(response.statusCode, getErrorMessage(response))

    private fun getErrorMessage(response: ClientHttpResponse) = when (response.statusCode) {
        HttpStatus.NOT_FOUND -> "Connection Error: The provided server is invalid"
        HttpStatus.UNAUTHORIZED -> "Authentication Error: Invalid email address or password"
        else -> response.statusText.orEmpty()
    }
}
