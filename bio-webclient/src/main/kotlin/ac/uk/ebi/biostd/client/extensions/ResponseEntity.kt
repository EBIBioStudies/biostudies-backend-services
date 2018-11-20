package ac.uk.ebi.biostd.client.extensions

import org.springframework.http.ResponseEntity

internal fun <T, S> ResponseEntity<T>.map(function: (T) -> S) = ResponseEntity(function(body), this.statusCode)
