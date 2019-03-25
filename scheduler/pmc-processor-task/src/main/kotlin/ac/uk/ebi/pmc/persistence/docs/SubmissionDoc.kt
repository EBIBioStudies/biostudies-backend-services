package ac.uk.ebi.pmc.persistence.docs

import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
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

    fun asInsertOrExpire(): Bson = Updates.combine(
        Updates.setOnInsert(Fields.accNo, accno),
        Updates.setOnInsert(Fields.body, body),
        Updates.setOnInsert(Fields.sourceFile, sourceFile),
        Updates.setOnInsert(Fields.posInFile, posInFile),
        Updates.setOnInsert(Fields.sourceTime, sourceTime),
        Updates.setOnInsert(Fields.status, status),
        Updates.setOnInsert(Fields.updated, updated),
        Updates.set(Fields.status, SubmissionStatus.DISCARDED)
    )

    companion object Fields {
        const val accNo = "accno"
        const val body = "body"
        const val status = "status"
        const val sourceFile = "sourceFile"
        const val posInFile = "posInFile"
        const val sourceTime = "sourceTime"
        const val updated = "updated"
        const val files = "files"
    }
}

enum class SubmissionStatus {
    LOADED, PROCESSING, PROCESSED, SUBMITTING, SUBMITTED, ERROR_LOAD, ERROR_PROCESS, ERROR_SUBMIT, DISCARDED
}
