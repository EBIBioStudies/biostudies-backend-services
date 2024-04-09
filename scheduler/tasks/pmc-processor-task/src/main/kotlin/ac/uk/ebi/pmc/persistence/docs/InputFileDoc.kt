package ac.uk.ebi.pmc.persistence.docs

import org.bson.types.ObjectId
import java.time.Instant

@Suppress("ConstructorParameterNaming")
data class InputFileDoc(
    val name: String,
    val loaded: Instant = Instant.now(),
    val status: InputFileStatus,
    val error: String?,
    val _id: ObjectId? = ObjectId(),
)

enum class InputFileStatus { PROCESSED, FAILED }
