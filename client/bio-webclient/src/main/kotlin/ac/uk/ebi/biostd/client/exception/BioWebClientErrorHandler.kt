package ac.uk.ebi.biostd.client.exception

import ebi.ac.uk.io.ext.asString
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

class BioWebClientErrorHandler : ResponseErrorHandler {
    override fun hasError(response: ClientHttpResponse) = response.statusCode.isError

    override fun handleError(response: ClientHttpResponse) {
        throw WebClientException(response.statusCode, errorMessage(response.body.asString(), response.statusCode))
    }

    private fun errorMessage(responseBody: String, status: HttpStatus) = when (status) {
        HttpStatus.NOT_FOUND -> "Connection Error: The provided server is invalid"
        HttpStatus.UNAUTHORIZED -> "Authentication Error: Invalid email address or password"
        else -> runCatching { JSONObject(responseBody).toString(2) }.getOrElse { responseBody }
    }
}
