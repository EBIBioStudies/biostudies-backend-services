package ac.uk.ebi.biostd.submission.web.exception

import mu.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import uk.ac.ebi.fire.client.exception.FireClientException

private val logger = KotlinLogging.logger {}

/**
 * Just like the REST resource, this is added here for testing purposes but once the client is fully integrated, these
 * kind of submissions should be handled as part of the inner application logic and this class should be removed. Final
 * users don't need to know about FIRE.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class FireExceptionHandler {
    @ExceptionHandler(FireClientException::class)
    fun handle(exception: FireClientException): ResponseEntity<String> {
        logger.error(exception) {}
        return ResponseEntity.status(exception.statusCode).body(exception.message)
    }
}
