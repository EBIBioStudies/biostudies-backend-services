package ac.uk.ebi.pmc.persistence.docs

import org.bson.types.ObjectId
import java.time.Instant

@Suppress("VariableNaming")
data class SubmissionDoc(
    val accno: String,
    var body: String,
    var status: SubmissionStatus,
    val sourceFile: String,
    val posInFile: Int,
    var sourceTime: Instant,
    var files: List<ObjectId> = emptyList(),
    var updated: Instant = Instant.now()
) {
    val _id: ObjectId? = null

    fun withStatus(status: SubmissionStatus): SubmissionDoc {
        this.status = status
        this.updated = Instant.now()
        return this
    }

    fun withBody(body: String): SubmissionDoc {
        this.body = body
        return this
    }

    companion object Fields {
        const val _id = "_id"
        const val accNo = "accno"
        const val status = "status"
        const val posInFile = "posInFile"
        const val sourceTime = "sourceTime"
        const val updated = "updated"
    }

    fun isNewerOrEqual(other: SubmissionDoc) =
        (other.accno == accno)
            .and((other.sourceTime > sourceTime)
                .or((other.sourceTime == sourceTime).and(other.posInFile >= posInFile)))
}

enum class SubmissionStatus {
    LOADED, PROCESSING, PROCESSED, SUBMITTING, SUBMITTED, ERROR_LOAD, ERROR_PROCESS, ERROR_SUBMIT, DISCARDED
}
