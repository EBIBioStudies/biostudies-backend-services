package ac.uk.ebi.biostd.submission.web.exception

import ac.uk.ebi.biostd.exception.EmptyPageTabFileException
import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ebi.ac.uk.errors.ValidationNode
import ebi.ac.uk.errors.ValidationNodeStatus.ERROR
import ebi.ac.uk.errors.ValidationTree
import ebi.ac.uk.errors.ValidationTreeStatus.FAIL
import mu.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

private val logger = KotlinLogging.logger {}

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ValidationExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidSubmissionException::class)
    fun handle(exception: InvalidSubmissionException): ValidationTree {
        logger.error(exception) {}
        val node = ValidationNode(
            ERROR,
            exception.message ?: exception.javaClass.name,
            exception.causes.map { ValidationNode(ERROR, it.message ?: it.javaClass.name) }
        )
        return ValidationTree(FAIL, node)
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EmptyPageTabFileException::class)
    fun handleRuntime(exception: EmptyPageTabFileException): ValidationTree {
        logger.error(exception) {}
        return ValidationTree(FAIL, ValidationNode(ERROR, exception.message ?: exception.javaClass.name))
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidFileListException::class)
    fun handleRuntime(exception: InvalidFileListException): ValidationTree {
        logger.error(exception) {}
        return ValidationTree(FAIL, ValidationNode(ERROR, exception.message ?: exception.javaClass.name))
    }
}
