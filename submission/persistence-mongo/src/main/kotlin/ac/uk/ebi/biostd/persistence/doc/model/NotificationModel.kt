package ac.uk.ebi.biostd.persistence.doc.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.time.Instant

@Document(collection = "notification_logs")
data class DocNotificationLog(
    @Id
    val id: ObjectId,
    val key: String,
    val date: Instant,
    val notificationType: String,
    val notification: Serializable,
)

@Document(collection = "notification_errors")
data class DocNotificationError(
    @Id
    val id: ObjectId,
    val key: String,
    val date: Instant,
    val errorMessage: String,
    val notificationType: String,
    val notification: Serializable,
)
