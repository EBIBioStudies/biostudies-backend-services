package ac.uk.ebi.biostd.persistence.common.service

interface NotificationErrorDataService {
    suspend fun saveNotificationError(
        key: String,
        messagePayload: String,
        notificationType: String,
        errorMessage: String,
    )
}
