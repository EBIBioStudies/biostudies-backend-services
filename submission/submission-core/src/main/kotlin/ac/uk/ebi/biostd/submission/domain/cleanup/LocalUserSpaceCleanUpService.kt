package ac.uk.ebi.biostd.submission.domain.cleanup

import ac.uk.ebi.biostd.common.properties.CleanUpProperties
import ac.uk.ebi.biostd.persistence.common.service.NotificationErrorDataService
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.extended.events.CleanUpNotification
import ebi.ac.uk.io.ext.isEmpty
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalUserSpaceCleanUpService(
    private val userRepository: UserDataRepository,
    private val cleanUpProperties: CleanUpProperties,
    private val securityQueryService: SecurityQueryService,
    private val eventsPublisherService: EventsPublisherService,
    private val notificationErrorService: NotificationErrorDataService,
) {
    suspend fun sendNotifications() {
        val today = LocalDate.now()
        notifyUsers(today.minusDays(cleanUpProperties.firstWarningDays), WARNING_SUBJECT, WARNING_TEMPLATE)
        notifyUsers(today.minusDays(cleanUpProperties.secondWarningDays), WARNING_SUBJECT, WARNING_TEMPLATE)
        notifyUsers(today.minusDays(cleanUpProperties.thirdWarningDays), FINAL_WARNING_SUBJECT, FINAL_WARNING_TEMPLATE)
    }

    private suspend fun notifyUsers(
        date: LocalDate,
        emailSubject: String,
        emailTemplate: String,
    ) = withContext(Dispatchers.IO) {
        logger.info { "Notifying cleanup for users with last activity at $date" }
        userRepository
            .findAllByLastActivityIsBetween(date.atStartOfDay(), date.atEndOfDay())
            .map { securityQueryService.getUser(it) }
            .filterNot { isUserSpaceEmpty(it) }
            .forEach { notifyCleanUp(it, emailSubject, emailTemplate) }
    }

    private suspend fun isUserSpaceEmpty(user: SecurityUser) =
        runCatching {
            user.userFolder.path
                .toFile()
                .isEmpty()
        }.onFailure {
            val error = "Error checking user folder for '${user.email}', secret: ${user.userFolder.path}"
            logger.error { error }
            notificationErrorService.saveNotificationError(user.email, error, CLEAN_UP_ERROR, it.localizedMessage)
        }.getOrDefault(true)

    private fun notifyCleanUp(
        user: SecurityUser,
        emailSubject: String,
        emailTemplate: String,
    ) {
        logger.info { "Notifying cleanup for user ${user.email} with last activity at ${user.lastActivity}" }
        val notification =
            CleanUpNotification(
                email = user.email,
                username = user.fullName,
                emailSubject = emailSubject,
                emailTemplate = emailTemplate,
                lastActivityDate = user.lastActivity.formatted(),
                cleanUpDate = user.lastActivity.plusDays(cleanUpProperties.cleanUpPeriodDays).formatted(),
            )
        eventsPublisherService.cleanupNotification(notification)
    }

    private fun LocalDateTime.formatted() = format(DateTimeFormatter.ofPattern(DATE_FORMAT))

    private fun LocalDate.atEndOfDay() = plusDays(1).atStartOfDay().minusSeconds(1)

    companion object {
        const val DATE_FORMAT = "dd MMMM yyyy"
        const val CLEAN_UP_ERROR = "User Space Clean Up"
        const val WARNING_TEMPLATE = "clean-up-warning"
        const val FINAL_WARNING_TEMPLATE = "clean-up-final-warning"
        const val WARNING_SUBJECT = "Inactivity notice – Cleanup of your BioStudies workspace"
        const val FINAL_WARNING_SUBJECT = "Final notice – Imminent cleanup of your BioStudies workspace"

        private val logger = KotlinLogging.logger {}
    }
}
