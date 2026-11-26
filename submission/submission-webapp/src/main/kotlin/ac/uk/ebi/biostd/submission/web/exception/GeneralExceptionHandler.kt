package ac.uk.ebi.biostd.submission.web.exception

import ac.uk.ebi.biostd.files.exception.UserFolderNotFound
import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import mu.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import uk.ac.ebi.biostd.errors.ValidationNode
import uk.ac.ebi.biostd.errors.ValidationNodeStatus.ERROR
import uk.ac.ebi.biostd.errors.ValidationTree
import uk.ac.ebi.biostd.errors.ValidationTreeStatus.FAIL

private val logger = KotlinLogging.logger {}

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class GeneralExceptionHandler {
    @ExceptionHandler(UserFolderNotFound::class)
    fun handleUserFolderNotFound(exception: UserFolderNotFound): ResponseEntity<ValidationTree> =
        ResponseEntity
            .internalServerError()
            // File downloads request application/octet-stream, so set the error format explicitly.
            .contentType(MediaType.APPLICATION_JSON)
            .body(handleRuntime(exception))

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntime(exception: RuntimeException): ValidationTree {
        logger.error(exception) {}
        return ValidationTree(FAIL, ValidationNode(ERROR, exception.message ?: exception.javaClass.name))
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidArgument(exception: MethodArgumentNotValidException): ValidationTree {
        logger.error(exception) {}
        val errors =
            exception
                .bindingResult
                .allErrors
                .map { ValidationNode(ERROR, it.defaultMessage ?: it.objectName) }

        return ValidationTree(FAIL, ValidationNode(ERROR, "Form Validation Errors", errors))
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = [SubmissionNotFoundException::class])
    fun handleSubmissionNotFound(exception: SubmissionNotFoundException): ValidationTree {
        logger.error(exception) {}
        return ValidationTree(FAIL, ValidationNode(ERROR, exception.message ?: exception.javaClass.name))
    }
}
