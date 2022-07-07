package uk.ac.ebi.scheduler.releaser.service

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ebi.ac.uk.util.date.asOffsetAtEndOfDay
import ebi.ac.uk.util.date.asOffsetAtStartOfDay
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.scheduler.releaser.config.NotificationTimes
import uk.ac.ebi.scheduler.releaser.model.ReleaseData
import uk.ac.ebi.scheduler.releaser.persistence.ReleaserRepository
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

private val logger = KotlinLogging.logger {}

class SubmissionReleaserService(
    private val bioWebClient: BioWebClient,
    private val notificationTimes: NotificationTimes,
    private val releaserRepository: ReleaserRepository,
    private val eventsPublisherService: EventsPublisherService
) {
    fun notifySubmissionReleases() {
        val today = Instant.now()
        notifyRelease(today.plus(notificationTimes.firstWarningDays, DAYS))
        notifyRelease(today.plus(notificationTimes.secondWarningDays, DAYS))
        notifyRelease(today.plus(notificationTimes.thirdWarningDays, DAYS))
    }

    fun releaseDailySubmissions() {
        val to = Instant.now().asOffsetAtEndOfDay()
        logger.info { "Releasing submissions up to $to" }

        releaserRepository
            .findAllUntil(to.toLocalDate())
            .forEach(::releaseSubmission)
    }

    fun generateFtpLinks() {
        releaserRepository
            .findAllReleased()
            .forEach(::generateFtpLink)
    }

    private fun releaseSubmission(releaseData: ReleaseData) {
        logger.info { "Releasing submission ${releaseData.accNo}" }
        bioWebClient.releaseSubmission(releaseData.asReleaseDto())
    }

    private fun notifyRelease(date: Instant) {
        val from = date.asOffsetAtStartOfDay()
        val to = date.asOffsetAtEndOfDay()

        logger.info { "Notifying submissions releases from $from to $to" }
        releaserRepository
            .findAllBetween(from.toLocalDate(), to.toLocalDate())
            .forEach(::notify)
    }

    private fun notify(releaseData: ReleaseData) {
        logger.info { "Notifying submission release for ${releaseData.accNo}" }
        eventsPublisherService.submissionReleased(releaseData.accNo, releaseData.owner)
    }

    private fun generateFtpLink(releaseData: ReleaseData) {
        logger.info { "Generating FTP link for submission ${releaseData.accNo}" }
        bioWebClient.generateFtpLink(releaseData.relPath)
    }
}
