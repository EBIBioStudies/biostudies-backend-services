package ac.uk.ebi.pmc.persistence.docs

import org.bson.types.ObjectId
import java.time.Instant

data class SubmissionDoc(
    val id: String,
    val body: String,
    val sourceFile: String,
    val files: List<ObjectId>,
    var status: SubStatus,
    var updated: Instant
) {

    val _id: ObjectId? = null

    fun withStatus(status: SubStatus): SubmissionDoc {
        this.status = status
        this.updated = Instant.now()
        return this
    }

    companion object {
        const val status = "status"
        const val sourceFile = "sourceFile"
        const val updated = "updated"
        const val imported = "imported"
    }
}

enum class SubStatus {
    LOADED, PROCESSING, PROCESED, SUBMITTING, SUBMITTED, ERROR
}
