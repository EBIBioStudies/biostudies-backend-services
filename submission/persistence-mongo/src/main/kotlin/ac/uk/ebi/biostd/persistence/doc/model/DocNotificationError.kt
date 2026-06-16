package ac.uk.ebi.biostd.persistence.doc.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "notification_errors")
data class DocNotificationError(
    @Id
    val id: ObjectId,
    val key: String,
    val messagePayload: String,
    val notificationType: String,
    val errorMessage: String,
    val date: Instant,
)
