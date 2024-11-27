package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.doc.model.CollectionNames.SUB_DRAFTS
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(SUB_DRAFTS)
data class DocSubmissionDraft(
    @Id
    val id: String,
    val owner: String,
    val key: String,
    val content: String,
    val status: DraftStatus,
    val modificationTime: Instant,
) {
    constructor(
        owner: String,
        key: String,
        content: String,
        status: DraftStatus,
        modificationTime: Instant,
    ) :
        this(
            ObjectId().toString(),
            owner,
            key,
            content,
            status,
            modificationTime,
        )

    companion object {
        const val OWNER = "owner"
        const val KEY = "key"
        const val CONTENT = "content"
        const val STATUS = "status"
        const val MODIFICATION_TIME = "modificationTime"
    }

    enum class DraftStatus {
        ACTIVE,
        PROCESSING,
        ACCEPTED,
    }
}
