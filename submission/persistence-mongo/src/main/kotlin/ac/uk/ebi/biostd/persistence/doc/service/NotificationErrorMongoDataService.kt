package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.service.NotificationErrorDataService
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.NotificationErrorMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocNotificationError
import org.bson.types.ObjectId
import java.time.Instant

class NotificationErrorMongoDataService(
    private val notificationErrorMongoRepository: NotificationErrorMongoRepository,
) : NotificationErrorDataService {
    override suspend fun saveNotificationError(
        key: String,
        messagePayload: String,
        notificationType: String,
        errorMessage: String,
    ) {
        notificationErrorMongoRepository.save(
            DocNotificationError(
                id = ObjectId(),
                key = key,
                messagePayload = messagePayload,
                notificationType = notificationType,
                errorMessage = errorMessage,
                date = Instant.now(),
            ),
        )
    }
}
