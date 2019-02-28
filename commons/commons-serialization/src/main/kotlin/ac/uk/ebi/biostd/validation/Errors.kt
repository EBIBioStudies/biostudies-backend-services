package ac.uk.ebi.biostd.validation

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import com.google.common.collect.Multimap
import ebi.ac.uk.model.Submission

const val INVALID_CHUNK_ERROR_MSG = "Invalid chunk: %s"
const val EMPTY_ELEMENT_ERROR_MSG = "At least one element must be provided"

class InvalidElementException(override val message: String) : RuntimeException(message)
class SerializationException(val submission: Submission, val errors: Multimap<Any, SerializationError>) : RuntimeException()
class SerializationError(val chunk: TsvChunk, val cause: Exception)
class InvalidChunkException(chunk: TsvChunk) : RuntimeException(String.format(INVALID_CHUNK_ERROR_MSG, chunk))
