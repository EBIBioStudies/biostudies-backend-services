package ac.uk.ebi.biostd.client.exception

import ebi.ac.uk.io.ext.asString
import org.json.JSONObject
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

class BioWebClientErrorHandler : ResponseErrorHandler {
    override fun hasError(response: ClientHttpResponse) = response.statusCode.isError

    override fun handleError(response: ClientHttpResponse) {
        val responseBody = response.body.asString()
        val errorMessage = if (isValidationTree(responseBody)) JSONObject(responseBody).toString(2) else responseBody

        throw WebClientException(response.statusCode, errorMessage)
    }

    private fun isValidationTree(response: String) = response.startsWith("{")
}
