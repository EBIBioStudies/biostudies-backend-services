package ac.uk.ebi.biostd.submission.web.exception

import ac.uk.ebi.biostd.persistence.exception.SubmissionNotFoundException
import ebi.ac.uk.errors.ValidationNode
import ebi.ac.uk.errors.ValidationNodeStatus
import ebi.ac.uk.errors.ValidationTree
import ebi.ac.uk.errors.ValidationTreeStatus
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class SubmissionExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = [SubmissionNotFoundException::class])
    fun handle(exception: SubmissionNotFoundException): ValidationTree {
        exception.printStackTrace()
        return ValidationTree(
            ValidationTreeStatus.FAIL,
            ValidationNode(ValidationNodeStatus.ERROR, exception.message ?: exception.javaClass.name)
        )
    }
}
