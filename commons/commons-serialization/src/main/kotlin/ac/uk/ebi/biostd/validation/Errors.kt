package ac.uk.ebi.biostd.validation

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import com.google.common.collect.Multimap
import ebi.ac.uk.model.Submission

const val CHUNK_SIZE_ERROR_MSG = "Exactly one element must be provided"

class InvalidElementException(message: String) : RuntimeException("$message. Element was not created.")
class SerializationException(val submission: Submission, val errors: Multimap<Any, SerializationError>)
    : RuntimeException()
class SerializationError(val chunk: TsvChunk, val cause: Exception)
class InvalidChunkSizeException : RuntimeException(CHUNK_SIZE_ERROR_MSG)
class IvalidSectionException(accNo: String) : RuntimeException(String.format(SECTION_NOT_CREATED, accNo))
