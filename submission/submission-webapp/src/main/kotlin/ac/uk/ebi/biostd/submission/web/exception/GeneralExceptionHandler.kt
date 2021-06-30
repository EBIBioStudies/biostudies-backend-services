package ac.uk.ebi.biostd.submission.web.exception

import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ebi.ac.uk.errors.ValidationNode
import ebi.ac.uk.errors.ValidationNodeStatus.ERROR
import ebi.ac.uk.errors.ValidationTree
import ebi.ac.uk.errors.ValidationTreeStatus.FAIL
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class GeneralExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntime(exception: RuntimeException): ValidationTree {
        exception.printStackTrace()
        return ValidationTree(FAIL, ValidationNode(ERROR, exception.message ?: exception.javaClass.name))
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidArgument(exception: MethodArgumentNotValidException): ValidationTree {
        val errors = exception
            .bindingResult
            .allErrors
            .map { ValidationNode(ERROR, it.defaultMessage ?: it.objectName) }

        return ValidationTree(FAIL, ValidationNode(ERROR, "Form Validation Errors", errors))
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = [SubmissionNotFoundException::class])
    fun handleSubmissionNotFound(exception: SubmissionNotFoundException): ValidationTree {
        exception.printStackTrace()
        return ValidationTree(FAIL, ValidationNode(ERROR, exception.message ?: exception.javaClass.name))
    }
}
