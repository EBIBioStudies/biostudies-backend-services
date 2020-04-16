package ac.uk.ebi.biostd.client.exception

import org.springframework.http.HttpStatus

class WebClientException(val statusCode: HttpStatus, message: String) : RuntimeException(message)

class SecurityWebClientException(val statusCode: HttpStatus, message: String) : RuntimeException(message)
