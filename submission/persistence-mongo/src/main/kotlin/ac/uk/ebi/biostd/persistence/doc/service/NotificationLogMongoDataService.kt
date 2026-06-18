package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.service.NotificationLogDataService
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.NotificationErrorMongoRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.NotificationLogMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocNotificationError
import ac.uk.ebi.biostd.persistence.doc.model.DocNotificationLog
import org.bson.types.ObjectId
import java.io.Serializable
import java.time.Instant

class NotificationLogMongoDataService(
    private val notificationLogMongoRepository: NotificationLogMongoRepository,
    private val notificationErrorMongoRepository: NotificationErrorMongoRepository,
) : NotificationLogDataService {
    override suspend fun logNotification(
        key: String,
        notificationType: String,
        notification: Serializable,
    ) {
        notificationLogMongoRepository.save(
            DocNotificationLog(
                id = ObjectId(),
                key = key,
                date = Instant.now(),
                notification = notification,
                notificationType = notificationType,
            ),
        )
    }

    override suspend fun logNotificationError(
        key: String,
        errorMessage: String,
        notificationType: String,
        notification: Serializable,
    ) {
        notificationErrorMongoRepository.save(
            DocNotificationError(
                id = ObjectId(),
                key = key,
                date = Instant.now(),
                errorMessage = errorMessage,
                notification = notification,
                notificationType = notificationType,
            ),
        )
    }
}
