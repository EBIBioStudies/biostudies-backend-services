package ac.uk.ebi.biostd.client.exception

import org.springframework.http.HttpStatusCode

class WebClientException(
    val statusCode: HttpStatusCode,
    message: String,
) : RuntimeException(message)
