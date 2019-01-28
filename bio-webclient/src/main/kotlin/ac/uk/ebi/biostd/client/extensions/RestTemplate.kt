package ac.uk.ebi.biostd.client.extensions

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

inline fun <reified T> RestTemplate.postForEntity(url: String, request: HttpEntity<*>): ResponseEntity<T> {
    return exchange(url, HttpMethod.POST, request, T::class.java)
}
