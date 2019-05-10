package ac.uk.ebi.biostd.submission.exception

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.validation.SerializationException
import ebi.ac.uk.errors.ValidationNode
import ebi.ac.uk.errors.ValidationTree
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class SerializationExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [SerializationException::class])
    fun handle(exception: SerializationException): ValidationTree {
        val node = ValidationNode("ERROR", "Error processing submission")
        node.subnodes.addAll(exception.errors.values().map { createErrorNode(getMessage(it.chunk, it.cause.message)) })
        return ValidationTree("ERROR", node)
    }

    fun getMessage(chunk: TsvChunk, message: String?) =
        "Error processing block starting in Lines [${chunk.startIndex}-${chunk.startIndex + chunk.endIndex}], $message"

    fun createErrorNode(message: String) = ValidationNode("ERROR", message)
}
