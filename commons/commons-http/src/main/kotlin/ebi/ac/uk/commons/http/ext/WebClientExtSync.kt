package ebi.ac.uk.commons.http.ext

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec

inline fun <reified T> WebClient.getForObject(url: String): T {
    return get().uri(url).retrieveBlocking<T>()!!
}

fun WebClient.post(url: String, params: RequestParams? = null) {
    post().retrieveBlocking<String>(url, params)
}

fun WebClient.delete(url: String) {
    delete().uri(url).retrieveBlocking<String>()
}

inline fun <reified T> RequestHeadersSpec<*>.retrieveBlocking(): T? {
    return retrieve().bodyToMono(T::class.java).block()
}

inline fun <reified T> WebClient.postForObject(url: String, params: RequestParams? = null): T {
    return post().retrieveBlocking<T>(url, params)!!
}

fun WebClient.put(url: String, params: RequestParams) {
    put().retrieveBlocking<String>(url, params)
}

inline fun <reified T> RequestBodyUriSpec.retrieveBlocking(url: String, params: RequestParams? = null): T? {
    val uriSpec = uri(url)
    params?.headers?.let { headers -> uriSpec.headers { it.addAll(headers) } }
    params?.body?.let { body -> uriSpec.bodyValue(body) }

    return uriSpec
        .retrieve()
        .bodyToMono(T::class.java)
        .block()
}
