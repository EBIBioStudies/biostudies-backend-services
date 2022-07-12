package ac.uk.ebi.biostd.client.extensions

import org.springframework.http.ResponseEntity

internal fun <T, S> ResponseEntity<T>.map(function: (T) -> S): ResponseEntity<S> {
    val body = requireNotNull(body) { "Response body expected not to be null." }
    return ResponseEntity(function(body), this.statusCode)
}
