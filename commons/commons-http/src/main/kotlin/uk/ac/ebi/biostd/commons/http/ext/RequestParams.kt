package uk.ac.ebi.biostd.commons.http.ext

import org.springframework.http.HttpHeaders

data class RequestParams(
    val headers: HttpHeaders? = null,
    val body: Any? = null,
)
