package ac.uk.ebi.biostd.persistence.doc.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("submission-drafts")
data class DocSubmissionDraft(
    @Id
    val id: String,
    val userId: Long,
    val key: String,
    val content: String
) {
    constructor(userId: Long, key: String, content: String) : this(ObjectId().toString(), userId, key, content)

    companion object {
        const val USER_ID = "userId"
        const val KEY = "key"
    }
}
