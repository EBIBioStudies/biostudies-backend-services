package ac.uk.ebi.biostd.client.exception

import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

fun bioWebClientErrorHandler(): ExchangeFilterFunction {
    fun errorMessage(responseBody: String, status: HttpStatus) = when (status) {
        HttpStatus.NOT_FOUND -> "Connection Error: The provided server is invalid"
        HttpStatus.UNAUTHORIZED -> "Authentication Error: Invalid email address or password"
        else -> runCatching { JSONObject(responseBody).toString(2) }.getOrElse { responseBody }
    }

    return ExchangeFilterFunction.ofResponseProcessor { response: ClientResponse ->
        val statusCode = response.statusCode()
        if (statusCode.isError) {
            return@ofResponseProcessor response.bodyToMono<String>().flatMap {
                Mono.error<ClientResponse>(WebClientException(statusCode, errorMessage(it, statusCode)))
            }
        }

        return@ofResponseProcessor Mono.just(response)
    }
}
