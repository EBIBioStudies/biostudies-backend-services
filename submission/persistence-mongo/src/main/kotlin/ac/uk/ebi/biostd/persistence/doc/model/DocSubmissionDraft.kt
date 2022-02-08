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
    val statusDraft: StatusDraft
) {
    constructor(userId: String, key: String, content: String, statusDraft: StatusDraft) :
        this(ObjectId().toString(), userId, key, content, statusDraft)

    companion object {
        const val USER_ID = "userId"
        const val KEY = "key"
        const val CONTENT = "content"
        const val STATUS_DRAFT = "statusDraft"
    }

    enum class StatusDraft {
        ACTIVE,
        PROCESSING
    }
}
