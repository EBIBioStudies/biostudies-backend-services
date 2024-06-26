package ac.uk.ebi.biostd.submission.exception

import ac.uk.ebi.biostd.submission.web.exception.SerializationExceptionHandler
import ac.uk.ebi.biostd.tsv.deserialization.model.FileChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import ac.uk.ebi.biostd.validation.SerializationError
import ac.uk.ebi.biostd.validation.SerializationException
import com.google.common.collect.HashMultimap
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.errors.ValidationNode
import ebi.ac.uk.errors.ValidationNodeStatus
import ebi.ac.uk.errors.ValidationNodeStatus.ERROR
import ebi.ac.uk.errors.ValidationTreeStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SerializationExceptionHandlerTest {
    private val testInstance = SerializationExceptionHandler()

    @Test
    fun handle() {
        val testSubmission = submission("ABC-123") {}
        val testErrors = HashMultimap.create<Any, SerializationError>()
        val testChunk = FileChunk(listOf(TsvChunkLine(1, emptyList()), TsvChunkLine(2, emptyList()), TsvChunkLine(3, emptyList())))
        testErrors.put(testChunk, SerializationError(testChunk, Exception("An exception")))

        val validation = testInstance.handle(SerializationException(testSubmission, testErrors))

        assertThat(validation.status).isEqualTo(ValidationTreeStatus.FAIL)
        assertValidationNode(validation.log, ERROR, "Error processing submission")

        assertThat(validation.log.subnodes).hasSize(1)
        assertValidationNode(
            validation.log.subnodes.first(),
            ERROR,
            "Error processing block starting in Lines [1-2], An exception",
        )
    }

    private fun assertValidationNode(
        node: ValidationNode,
        expectedLevel: ValidationNodeStatus,
        expectedMessage: String,
    ) {
        assertThat(node.level).isEqualTo(expectedLevel)
        assertThat(node.message).isEqualTo(expectedMessage)
    }
}
