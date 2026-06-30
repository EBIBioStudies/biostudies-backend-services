package ac.uk.ebi.biostd.persistence.common.service

import java.io.Serializable

interface NotificationLogDataService {
    suspend fun logNotification(
        key: String,
        notificationType: String,
        notification: Serializable,
    )

    suspend fun logNotificationError(
        key: String,
        errorMessage: String,
        notificationType: String,
        notification: Serializable,
    )
}
