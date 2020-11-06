package uk.ac.ebi.scheduler.releaser.service

import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.extensions.getExtSubmissionsAsSequence
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.isProject
import ebi.ac.uk.util.date.asOffsetAtEndOfDay
import ebi.ac.uk.util.date.asOffsetAtStartOfDay
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.scheduler.releaser.config.NotificationTimes
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

private val logger = KotlinLogging.logger {}

class SubmissionReleaserService(
    private val bioWebClient: BioWebClient,
    private val notificationTimes: NotificationTimes,
    private val eventsPublisherService: EventsPublisherService
) {
    fun notifySubmissionReleases() {
        val today = Instant.now()
        notifyRelease(today.plus(notificationTimes.firstWarningDays, DAYS))
        notifyRelease(today.plus(notificationTimes.secondWarningDays, DAYS))
        notifyRelease(today.plus(notificationTimes.thirdWarningDays, DAYS))
    }

    fun releaseDailySubmissions() {
        val today = Instant.now()
        logger.info { "releasing from ${ today.asOffsetAtStartOfDay() } to ${ today.asOffsetAtEndOfDay() }" }
        val query = ExtPageQuery(fromRTime = today.asOffsetAtStartOfDay(), toRTime = today.asOffsetAtEndOfDay())
        bioWebClient.getExtSubmissionsAsSequence(query).forEach(::releaseSubmission)
    }

    private fun releaseSubmission(extSubmission: ExtSubmission) {
        if (extSubmission.isProject.not().and(extSubmission.released.not())) {
            logger.info { "Releasing submission ${extSubmission.accNo}" }
            bioWebClient.submitExt(extSubmission.copy(released = true))
        }
    }

    private fun notifyRelease(date: Instant) {
        logger.info { "notifying from ${ date.asOffsetAtStartOfDay() } to ${ date.asOffsetAtEndOfDay() }" }
        val query = ExtPageQuery(fromRTime = date.asOffsetAtStartOfDay(), toRTime = date.asOffsetAtEndOfDay())
        bioWebClient.getExtSubmissionsAsSequence(query).forEach(::notify)
    }

    private fun notify(extSubmission: ExtSubmission) {
        logger.info { "Notifying submission release for ${extSubmission.accNo}" }
        eventsPublisherService.submissionReleased(extSubmission)
    }
}
