package uk.ac.ebi.fire.client.exception

import ebi.ac.uk.io.ext.asString
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

class FireWebClientErrorHandler : ResponseErrorHandler {
    override fun hasError(response: ClientHttpResponse): Boolean = response.statusCode.isError

    override fun handleError(response: ClientHttpResponse) =
        throw FireClientException(response.statusCode, response.body.asString())
}
