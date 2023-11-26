package uk.ac.ebi.scheduler.releaser.service

import ac.uk.ebi.biostd.client.dto.ReleaseRequestDto
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionReleaserRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.ReleaseData
import ebi.ac.uk.util.date.asOffsetAtEndOfDay
import ebi.ac.uk.util.date.asOffsetAtStartOfDay
import ebi.ac.uk.util.date.toDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.scheduler.releaser.config.NotificationTimes
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

private val logger = KotlinLogging.logger {}

class SubmissionReleaserService(
    private val bioWebClient: BioWebClient,
    private val notificationTimes: NotificationTimes,
    private val releaserRepository: SubmissionReleaserRepository,
    private val eventsPublisherService: EventsPublisherService,
) {
    suspend fun notifySubmissionReleases() {
        val today = Instant.now()
        notifyRelease(today.plus(notificationTimes.firstWarningDays, DAYS))
        notifyRelease(today.plus(notificationTimes.secondWarningDays, DAYS))
        notifyRelease(today.plus(notificationTimes.thirdWarningDays, DAYS))
    }

    suspend fun releaseDailySubmissions() {
        val to = Instant.now().asOffsetAtEndOfDay()
        logger.info { "Releasing submissions up to $to" }

        withContext(Dispatchers.Default) {
            releaserRepository
                .findAllUntil(to.toDate())
                .map { async { releaseSafely(it) } }
                .collect { it.await() }
        }
    }

    suspend fun generateFtpLinks() {
        withContext(Dispatchers.Default) {
            releaserRepository
                .findAllReleased()
                .map { async { generateFtpLinks(it) } }
                .collect { it.await() }
        }
    }

    private fun releaseSafely(releaseData: ReleaseData) {
        runCatching {
            bioWebClient.releaseSubmission(releaseData.asReleaseDto())
        }
            .onFailure { logger.info { "Failed to release submission ${releaseData.accNo}" } }
            .onSuccess { logger.info { "Released submission ${releaseData.accNo}" } }
    }

    private fun ReleaseData.asReleaseDto() = ReleaseRequestDto(accNo, owner, relPath)

    private suspend fun notifyRelease(date: Instant) {
        val from = date.asOffsetAtStartOfDay()
        val to = date.asOffsetAtEndOfDay()

        logger.info { "Notifying submissions releases from $from to $to" }

        withContext(Dispatchers.Default) {
            releaserRepository
                .findAllBetween(from.toDate(), to.toDate())
                .map { async { notify(it) } }
                .collect { it.await() }
        }
    }

    private fun notify(releaseData: ReleaseData) {
        logger.info { "Notifying submission release for ${releaseData.accNo}" }
        eventsPublisherService.subToBePublished(releaseData.accNo, releaseData.owner)
    }

    private suspend fun generateFtpLinks(releaseData: ReleaseData) {
        logger.info { "Generating FTP links for submission ${releaseData.accNo}" }
        bioWebClient.generateFtpLinks(releaseData.accNo)
    }
}
