package ac.uk.ebi.biostd.submission.web.exception

import ac.uk.ebi.biostd.submission.exceptions.ValidationException
import ebi.ac.uk.errors.ValidationNode
import ebi.ac.uk.errors.ValidationNodeStatus.ERROR
import ebi.ac.uk.errors.ValidationTree
import ebi.ac.uk.errors.ValidationTreeStatus.FAIL
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ValidationExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [ValidationException::class])
    fun handle(exception: ValidationException): ValidationTree {
        val node = ValidationNode(ERROR, exception.message ?: exception.javaClass.name, exception.causes)
        return ValidationTree(FAIL, node)
    }

}
