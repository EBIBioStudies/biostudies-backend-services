package ac.uk.ebi.pmc.persistence.docs

import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import java.time.Instant

@Suppress("ConstructorParameterNaming")
data class SubmissionDoc(
    val accNo: String,
    var body: String,
    var status: SubmissionStatus,
    val sourceFile: String,
    val posInFile: Int,
    var sourceTime: Instant,
    var files: List<ObjectId> = emptyList(),
    var updated: Instant = Instant.now(),
    val _id: ObjectId = ObjectId(),
) {
    fun withStatus(status: SubmissionStatus): SubmissionDoc {
        this.status = status
        this.updated = Instant.now()
        return this
    }

    fun withBody(body: String): SubmissionDoc {
        this.body = body
        return this
    }

    fun asInsertOrExpire(): Bson =
        Updates.combine(
            Updates.setOnInsert(SUB_ACC_NO, accNo),
            Updates.setOnInsert(SUB_ID, _id),
            Updates.setOnInsert(SUB_BODY, body),
            Updates.setOnInsert(SUB_SOURCE_FILE, sourceFile),
            Updates.setOnInsert(SUB_POS_IN_FILE, posInFile),
            Updates.setOnInsert(SUB_SOURCE_TIME, sourceTime),
            Updates.setOnInsert(SUB_FILES, files),
            Updates.setOnInsert(SUB_STATUS, status),
            Updates.setOnInsert(SUB_UPDATED, updated),
        )

    companion object Fields {
        const val SUB_ID = "_id"
        const val SUB_ACC_NO = "accNo"
        const val SUB_BODY = "body"
        const val SUB_STATUS = "status"
        const val SUB_SOURCE_FILE = "sourceFile"
        const val SUB_POS_IN_FILE = "posInFile"
        const val SUB_SOURCE_TIME = "sourceTime"
        const val SUB_UPDATED = "updated"
        const val SUB_FILES = "files"
    }
}

enum class SubmissionStatus {
    LOADED,
    PROCESSING,
    PROCESSED,
    SUBMITTING,
    SUBMITTED,
    ERROR_LOAD,
    ERROR_PROCESS,
    ERROR_SUBMIT,
    DISCARDED,
}
