package ebi.ac.uk.commons.http.ext

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange

suspend inline fun <reified T> WebClient.getForObjectAsync(url: String): T {
    return get().uri(url).retrieveAsync<T>()!!
}

suspend inline fun <reified T> WebClient.postForObjectAsync(url: String, params: RequestParams? = null): T {
    return post().retrieveAsync<T>(url, params)!!
}

suspend inline fun <reified T> WebClient.putForObjectAsync(url: String, params: RequestParams? = null): T {
    return put().retrieveAsync<T>(url, params)!!
}

suspend fun WebClient.deleteAsync(url: String) {
    val response = delete().uri(url).awaitExchange()
    require(response.statusCode().isError.not()) { response.toString() }
}

suspend inline fun <reified T> RequestHeadersSpec<*>.retrieveAsync(): T {
    return retrieve().awaitBody()
}

suspend inline fun <reified T> RequestBodyUriSpec.retrieveAsync(url: String, params: RequestParams? = null): T {
    val uriSpec = uri(url)
    params?.headers?.let { headers -> uriSpec.headers { it.addAll(headers) } }
    params?.body?.let { body -> uriSpec.bodyValue(body) }

    return uriSpec
        .retrieve()
        .awaitBody()
}
