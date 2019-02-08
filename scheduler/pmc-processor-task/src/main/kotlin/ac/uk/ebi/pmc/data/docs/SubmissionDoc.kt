package ac.uk.ebi.pmc.data.docs

import org.bson.types.ObjectId
import java.time.Instant

data class SubmissionDoc(
    val id: String,
    val body: String,
    val sourceFile: String,
    val files: List<ObjectId>,
    var submissionStatus: SubStatus,
    var updated: Instant
) {

    val _id: ObjectId? = null

    companion object {
        const val status = "status"
        const val sourceFile = "sourceFile"
        const val updated = "updated"
        const val imported = "imported"
    }
}

enum class SubStatus {
    LOADED, DOWNLOADING, DOWNLOADED, SUBMITTING, SUBMITTED, ERROR
}
