package ac.uk.ebi.biostd.persistence.doc.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDateTime

@Document(collection = "cleanup_logs")
data class DocCleanUpLog(
    @Id
    val id: ObjectId,
    val email: String,
    val date: Instant,
    val jobId: String,
    val lastActivity: LocalDateTime,
    val userSpacePath: String,
)

@Document(collection = "cleanup_errors")
data class DocCleanUpError(
    @Id
    val id: ObjectId,
    val email: String,
    val date: Instant,
    val errorMessage: String,
    val jobId: String?,
    val userSpacePath: String?,
)
