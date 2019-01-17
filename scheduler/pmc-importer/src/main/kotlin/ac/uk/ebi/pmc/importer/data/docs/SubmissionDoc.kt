package ac.uk.ebi.pmc.importer.data.docs

import org.bson.types.ObjectId
import java.time.Instant

data class SubmissionDoc(
    val id: String,
    val body: String,
    val sourceFile: String,
    val files: List<ObjectId>,
    val uploaded: Instant = Instant.now(),
    val imported: Boolean = false
)