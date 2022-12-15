package ac.uk.ebi.biostd.validation

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import com.google.common.collect.Multimap
import ebi.ac.uk.model.Submission

const val CHUNK_SIZE_ERROR_MSG = "Several page tab elements detected. Exactly one element must be provided"

class InvalidElementException(message: String) : RuntimeException(message)

class SerializationError(val chunk: TsvChunk, val cause: Exception)

class InvalidChunkSizeException : RuntimeException(CHUNK_SIZE_ERROR_MSG)

class InvalidSectionException(accNo: String) : RuntimeException(String.format(SECTION_NOT_CREATED, accNo))

class DuplicatedSectionAccNoException(accNo: String) : RuntimeException("A section with accNo $accNo already exists")

class SerializationException(
    val submission: Submission,
    val errors: Multimap<Any, SerializationError>
) : RuntimeException()
