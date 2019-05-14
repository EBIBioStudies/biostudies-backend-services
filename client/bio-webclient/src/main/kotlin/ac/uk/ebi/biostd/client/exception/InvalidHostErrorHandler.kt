package ac.uk.ebi.biostd.client.exception

import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestClientResponseException

class InvalidHostErrorHandler : ResponseErrorHandler {
    override fun hasError(response: ClientHttpResponse): Boolean {
        return response.statusCode == HttpStatus.NOT_FOUND
    }

    override fun handleError(response: ClientHttpResponse) =
        throw RestClientResponseException("Invalid host", HttpStatus.NOT_FOUND.value(), "Not found", null, null, null)
}
