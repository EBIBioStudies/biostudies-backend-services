package ac.uk.ebi.biostd.files.web.exception

import ac.uk.ebi.biostd.files.exception.FileAlreadyExistsException
import ac.uk.ebi.biostd.files.exception.FileNotFoundException
import ac.uk.ebi.biostd.files.exception.FileOperationException
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
class FileExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(FileNotFoundException::class)
    fun handleFileNotFound(exception: FileNotFoundException): ValidationTree {
        logger.error(exception) {}
        return ValidationTree(FAIL, ValidationNode(ERROR, exception.message))
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(FileAlreadyExistsException::class)
    fun handleFileAlreadyExists(exception: FileAlreadyExistsException): ValidationTree {
        logger.error(exception) {}
        return ValidationTree(FAIL, ValidationNode(ERROR, exception.message))
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(FileOperationException::class)
    fun handleFileOperation(exception: FileOperationException): ValidationTree {
        logger.error(exception) {}
        return ValidationTree(FAIL, ValidationNode(ERROR, exception.message))
    }
}
