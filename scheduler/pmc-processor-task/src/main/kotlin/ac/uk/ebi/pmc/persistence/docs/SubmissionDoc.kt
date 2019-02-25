package ac.uk.ebi.pmc.persistence.docs

import org.bson.types.ObjectId
import java.time.Instant

data class SubmissionDoc(
    val accNo: String,
    val body: String,
    var status: SubmissionStatus,
    val sourceFile: String,
    var sourceTime: Instant? = null,
    var files: List<ObjectId> = emptyList(),
    var updated: Instant = Instant.now()
) {

    val _id: ObjectId? = null

    fun withStatus(status: SubmissionStatus): SubmissionDoc {
        this.status = status
        this.updated = Instant.now()
        return this
    }

    companion object Fields {
        const val accNo = "accNo"
        const val status = "status"
        const val sourceFile = "sourceFile"
        const val sourceTime = "sourceTime"
        const val updated = "updated"
        const val imported = "imported"
    }
}

enum class SubmissionStatus {
    LOADED, PROCESSING, PROCESSED, SUBMITTING, SUBMITTED, ERROR_LOAD, ERROR_PROCESS, ERROR_SUBMIT, DISCARDED
}
