package ac.uk.ebi.biostd.submission.web.exception

import ebi.ac.uk.errors.ValidationNode
import ebi.ac.uk.errors.ValidationNodeStatus.ERROR
import ebi.ac.uk.errors.ValidationTree
import ebi.ac.uk.errors.ValidationTreeStatus.FAIL
import ebi.ac.uk.security.integration.exception.SecurityException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class SecurityExceptionWebHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(SecurityException::class)
    fun handle(exception: SecurityException): ValidationTree =
        ValidationTree(FAIL, ValidationNode(ERROR, exception.message))
}
