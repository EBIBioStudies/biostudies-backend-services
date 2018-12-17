package ac.uk.ebi.biostd.validation

import com.google.common.collect.Multimap
import ebi.ac.uk.model.Submission

class InvalidElementException(override val message: String) : RuntimeException(message)
class SerializationException(val submission: Submission, val errors: Multimap<Any, SerializationError>) : RuntimeException()
class SerializationError(val cause: Exception)
