package ac.uk.ebi.biostd.submission.web.exception

import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ebi.ac.uk.errors.ValidationNode
import ebi.ac.uk.errors.ValidationNodeStatus.ERROR
import ebi.ac.uk.errors.ValidationTree
import ebi.ac.uk.errors.ValidationTreeStatus.FAIL
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ValidationExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidSubmissionException::class)
    fun handle(exception: InvalidSubmissionException): ValidationTree {
        val node = ValidationNode(
            ERROR,
            exception.message ?: exception.javaClass.name,
            exception.causes.map { ValidationNode(ERROR, it.message ?: it.javaClass.name) }
        )

        return ValidationTree(FAIL, node)
    }
}
