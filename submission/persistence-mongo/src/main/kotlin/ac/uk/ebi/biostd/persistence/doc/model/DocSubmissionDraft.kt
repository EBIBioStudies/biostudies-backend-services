package ac.uk.ebi.biostd.persistence.doc.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("submission_drafts")
data class DocSubmissionDraft(
    @Id
    val id: String,
    val userId: String,
    val key: String,
    val content: String,
    val status: DraftStatus,
) {
    constructor(userId: String, key: String, content: String, status: DraftStatus) :
        this(ObjectId().toString(), userId, key, content, status)

    companion object {
        const val USER_ID = "userId"
        const val KEY = "key"
        const val CONTENT = "content"
        const val STATUS = "status"
    }

    enum class DraftStatus {
        ACTIVE,
        PROCESSING,
        ACCEPTED,
    }
}
