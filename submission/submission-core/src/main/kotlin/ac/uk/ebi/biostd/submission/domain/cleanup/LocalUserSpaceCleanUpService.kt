package ac.uk.ebi.biostd.submission.domain.cleanup

import ac.uk.ebi.biostd.common.properties.CleanUpProperties
import ac.uk.ebi.biostd.persistence.doc.service.NotificationErrorDataService
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
import java.time.ZoneOffset.UTC

class LocalUserSpaceCleanUpService(
    private val userRepository: UserDataRepository,
    private val cleanUpProperties: CleanUpProperties,
    private val securityQueryService: SecurityQueryService,
    private val eventsPublisherService: EventsPublisherService,
    private val notificationErrorService: NotificationErrorDataService,
) {
    suspend fun sendNotifications() {
        val today = LocalDate.now()
        notifyUsers(today.minusDays(cleanUpProperties.firstWarningDays))
        notifyUsers(today.minusDays(cleanUpProperties.secondWarningDays))
        notifyUsers(today.minusDays(cleanUpProperties.thirdWarningDays))
    }

    private suspend fun notifyUsers(date: LocalDate) =
        withContext(Dispatchers.IO) {
            val start = date.atStartOfDay()
            val end = date.atEndOfDay()
            logger.info { "Notifying cleanup for users with last activity from $start to $end" }
            userRepository
                .findAllByLastActivityIsBetween(start, end)
                .map { securityQueryService.getUser(it) }
                .filterNot { isUserSpaceEmpty(it) }
                .forEach {
                    notifyCleanUp(
                        email = it.email,
                        username = it.fullName,
                        lastActivityDate = it.lastActivity,
                        cleanUpDate = it.lastActivity.plusDays(cleanUpProperties.cleanUpPeriodDays),
                    )
                }
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
        email: String,
        username: String,
        lastActivityDate: LocalDateTime,
        cleanUpDate: LocalDateTime,
    ) {
        logger.info { "Notifying cleanup for user $email with last activity date $lastActivityDate" }
        eventsPublisherService.cleanupNotification(
            CleanUpNotification(
                email = email,
                username = username,
                lastActivityDate = lastActivityDate.formatted(),
                cleanUpDate = cleanUpDate.formatted(),
            ),
        )
    }

    private fun LocalDateTime.formatted() = atZone(UTC).toLocalDate().toString()

    private fun LocalDate.atEndOfDay() = plusDays(1).atStartOfDay().minusSeconds(1)

    companion object {
        const val CLEAN_UP_ERROR = "User Space Clean Up"

        private val logger = KotlinLogging.logger {}
    }
}
