package ac.uk.ebi.biostd.submission.web.exception

import ac.uk.ebi.biostd.exception.InvalidFormatException
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.validation.SerializationException
import com.fasterxml.jackson.databind.JsonMappingException
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
class SerializationExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [SerializationException::class])
    fun handle(exception: SerializationException): ValidationTree {
        logger.error(exception) {}
        val node = ValidationNode(ERROR, "Error processing submission", getErrors(exception))
        return ValidationTree(FAIL, node)
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(value = [InvalidFormatException::class])
    fun handle(exception: InvalidFormatException): ValidationTree {
        logger.error(exception) {}
        val node = ValidationNode(ERROR, exception.message!!)
        return ValidationTree(FAIL, node)
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = [JsonMappingException::class])
    fun handle(exception: JsonMappingException): ValidationTree {
        logger.error(exception) {}
        val node = ValidationNode(ERROR, exception.message ?: exception.localizedMessage)
        return ValidationTree(FAIL, node)
    }

    fun getErrors(exception: SerializationException): List<ValidationNode> =
        exception.errors.values().map { createErrorNode(getMessage(it.chunk, it.cause.message)) }

    fun getMessage(
        chunk: TsvChunk,
        message: String?,
    ) = "Error processing block starting in Lines [${chunk.startIndex}-${chunk.startIndex + chunk.endIndex}], $message"

    fun createErrorNode(message: String) = ValidationNode(ERROR, message)
}
