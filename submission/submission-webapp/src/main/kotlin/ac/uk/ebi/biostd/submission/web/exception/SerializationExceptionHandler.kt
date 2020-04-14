package ac.uk.ebi.biostd.submission.web.exception

import ac.uk.ebi.biostd.exception.InvalidExtensionException
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.validation.SerializationException
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
class SerializationExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [SerializationException::class])
    fun handle(exception: SerializationException): ValidationTree {
        val node = ValidationNode(ERROR, "Error processing submission", getErrors(exception))
        return ValidationTree(FAIL, node)
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(value = [InvalidExtensionException::class])
    fun handle(exception: InvalidExtensionException): ValidationTree {
        val node = ValidationNode(ERROR, exception.message)
        return ValidationTree(FAIL, node)
    }

    fun getErrors(exception: SerializationException): List<ValidationNode> =
        exception.errors.values().map { createErrorNode(getMessage(it.chunk, it.cause.message)) }

    fun getMessage(chunk: TsvChunk, message: String?) =
        "Error processing block starting in Lines [${chunk.startIndex}-${chunk.startIndex + chunk.endIndex}], $message"

    fun createErrorNode(message: String) = ValidationNode(ERROR, message)
}
