package ebi.ac.uk.commons.http.ext

import org.springframework.http.HttpHeaders

data class RequestParams(
    val headers: HttpHeaders? = null,
    val body: Any? = null
)
