package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.NotificationErrorMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocNotificationError
import org.bson.types.ObjectId
import java.time.Instant

interface NotificationErrorDataService {
    suspend fun saveNotificationError(
        accNo: String,
        messagePayload: String,
        notificationType: String,
        errorMessage: String,
    )
}

class NotificationErrorMongoDataService(
    private val notificationErrorMongoRepository: NotificationErrorMongoRepository,
) : NotificationErrorDataService {
    override suspend fun saveNotificationError(
        accNo: String,
        messagePayload: String,
        notificationType: String,
        errorMessage: String,
    ) {
        notificationErrorMongoRepository.save(
            DocNotificationError(
                id = ObjectId(),
                accNo = accNo,
                messagePayload = messagePayload,
                notificationType = notificationType,
                errorMessage = errorMessage,
                date = Instant.now(),
            ),
        )
    }
}
