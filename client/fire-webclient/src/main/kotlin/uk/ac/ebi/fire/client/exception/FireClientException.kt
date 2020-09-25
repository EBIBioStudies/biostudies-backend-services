package uk.ac.ebi.fire.client.exception

import org.springframework.http.HttpStatus

class FireClientException(val statusCode: HttpStatus, message: String) : RuntimeException(message)
