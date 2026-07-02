package ac.uk.ebi.biostd.submission.domain.cleanup

import ac.uk.ebi.biostd.common.properties.CleanUpProperties
import ac.uk.ebi.biostd.persistence.common.service.CleanUpLogDataService
import ac.uk.ebi.biostd.persistence.common.service.NotificationLogDataService
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.NotificationType.FINAL_WARNING
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.NotificationType.FIRST_WARNING
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.NotificationType.SECOND_WARNING
import ebi.ac.uk.extended.events.CleanUpNotification
import ebi.ac.uk.io.ext.isEmpty
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.events.service.EventsPublisherService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.absolutePathString

class LocalUserSpaceCleanUpService(
    private val clusterClient: ClusterClient,
    private val userRepository: UserDataRepository,
    private val cleanUpProperties: CleanUpProperties,
    private val securityQueryService: SecurityQueryService,
    private val cleanUpLogDataService: CleanUpLogDataService,
    private val eventsPublisherService: EventsPublisherService,
    private val notificationLogDataService: NotificationLogDataService,
) {
    suspend fun sendNotifications() {
        val today = LocalDate.now()
        notifyUsers(today.minusDays(cleanUpProperties.firstWarningDays), FIRST_WARNING)
        notifyUsers(today.minusDays(cleanUpProperties.secondWarningDays), SECOND_WARNING)
        notifyUsers(today.minusDays(cleanUpProperties.thirdWarningDays), FINAL_WARNING)
    }

    private suspend fun notifyUsers(
        date: LocalDate,
        type: NotificationType,
    ) = withContext(Dispatchers.IO) {
        logger.info { "Sending ${type.name} cleanup notifications for users with last activity at $date" }
        userRepository
            .findAllByLastActivityIsBetweenAndActive(date.atStartOfDay(), date.atEndOfDay())
            .map { securityQueryService.getUser(it) }
            .filterNot { isUserSpaceEmpty(it) }
            .forEach { notifyCleanUp(it, type) }
    }

    private suspend fun isUserSpaceEmpty(user: SecurityUser) =
        runCatching {
            user.userFolder.path
                .toFile()
                .isEmpty()
        }.onFailure {
            val error = "Error checking user folder for '${user.email}', secret: ${user.userFolder.path}"
            logger.error { error }
            notificationLogDataService.logNotificationError(user.email, it.localizedMessage, USER_SPACE_ERROR, error)
        }.getOrDefault(true)

    private fun notifyCleanUp(
        user: SecurityUser,
        type: NotificationType,
    ) {
        logger.info { "${type.name} notification for user ${user.email} with last activity at ${user.lastActivity}" }
        val cleanUpDate = user.lastActivity.plusDays(cleanUpProperties.cleanUpPeriodDays)
        val notification =
            CleanUpNotification(
                type = type.name,
                email = user.email,
                username = user.fullName,
                emailSubject = type.emailSubject,
                emailTemplate = type.emailTemplate,
                cleanUpDate = cleanUpDate.formatted(),
                lastActivityDate = user.lastActivity.formatted(),
            )
        eventsPublisherService.cleanupNotification(notification)
    }

    suspend fun cleanUpUserSpaces() =
        withContext(Dispatchers.IO) {
            val cleanUpDate = LocalDate.now().minusDays(cleanUpProperties.cleanUpPeriodDays)
            logger.info { "Cleaning up users with last activity at $cleanUpDate" }
            userRepository
                .findAllByLastActivityIsBetweenAndActive(cleanUpDate.atStartOfDay(), cleanUpDate.atEndOfDay())
                .map { securityQueryService.getUser(it) }
                .filterNot { isUserSpaceEmpty(it) }
                .forEach { cleanUpUserSpace(it) }
        }

    private suspend fun cleanUpUserSpace(user: SecurityUser) {
        val path = user.userFolder.path.absolutePathString()
        logger.info { "Dispatching user space clean up job for user ${user.email} at $path" }
        val job = JobSpec(queue = DataMoverQueue, command = "rm -rf $path/*")
        val jobTry = clusterClient.triggerJobAsync(job)
        jobTry.fold({
            cleanUpLogDataService.logCleanUp(user.email, it.id, user.lastActivity, path)
            logger.info { "User space clean up job dispatched for user ${user.email}. Job id: ${it.id}" }
        }, {
            cleanUpLogDataService.logCleanUpError(user.email, it.message ?: it.localizedMessage, path)
            logger.error { "Failed to dispatch user space clean up job for user ${user.email}. Error: ${it.message}" }
        })
    }

    private fun LocalDateTime.formatted() = format(DateTimeFormatter.ofPattern(DATE_FORMAT))

    private fun LocalDate.atEndOfDay() = plusDays(1).atStartOfDay().minusSeconds(1)

    companion object {
        const val DATE_FORMAT = "dd MMMM yyyy"
        const val USER_SPACE_ERROR = "USER_SPACE_ERROR"
        const val WARNING_TEMPLATE = "clean-up-warning"
        const val FINAL_WARNING_TEMPLATE = "clean-up-final-warning"
        const val WARNING_SUBJECT = "Inactivity notice – Cleanup of your BioStudies workspace"
        const val FINAL_WARNING_SUBJECT = "Final notice – Imminent cleanup of your BioStudies workspace"

        private val logger = KotlinLogging.logger {}
    }

    internal enum class NotificationType(
        val emailSubject: String,
        val emailTemplate: String,
    ) {
        FIRST_WARNING(WARNING_SUBJECT, WARNING_TEMPLATE),
        SECOND_WARNING(WARNING_SUBJECT, WARNING_TEMPLATE),
        FINAL_WARNING(FINAL_WARNING_SUBJECT, FINAL_WARNING_TEMPLATE),
    }
}
