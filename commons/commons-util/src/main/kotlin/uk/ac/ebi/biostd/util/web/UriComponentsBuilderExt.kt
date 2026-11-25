package uk.ac.ebi.biostd.util.web

import org.springframework.web.util.UriComponentsBuilder

fun UriComponentsBuilder.optionalQueryParam(
    name: String,
    value: Any?,
): UriComponentsBuilder {
    value?.let { queryParam(name, it) }
    return this
}
