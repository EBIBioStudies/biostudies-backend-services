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
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

class SubmissionReleaserService(
    private val bioWebClient: BioWebClient,
    private val notificationTimes: NotificationTimes,
    private val eventsPublisherService: EventsPublisherService
) {
    fun notifySubmissionReleases() {
        val today = LocalDate.now()
        logger.info {
            "first: ${notificationTimes.firstWarningDays} at ${today.plusDays(notificationTimes.firstWarningDays)}" }
        logger.info {
            "second: ${notificationTimes.secondWarningDays} at ${today.plusDays(notificationTimes.secondWarningDays)}" }
        logger.info {
            "third: ${notificationTimes.thirdWarningDays} at ${today.plusDays(notificationTimes.thirdWarningDays)}" }
        notifyRelease(today.plusDays(notificationTimes.firstWarningDays))
        notifyRelease(today.plusDays(notificationTimes.secondWarningDays))
        notifyRelease(today.plusDays(notificationTimes.thirdWarningDays))
    }

    fun releaseDailySubmissions() {
        val today = LocalDate.now()
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

    private fun notifyRelease(date: LocalDate) {
        logger.info { "notifying from ${ date.asOffsetAtStartOfDay() } to ${ date.asOffsetAtEndOfDay() }" }
        val query = ExtPageQuery(fromRTime = date.asOffsetAtStartOfDay(), toRTime = date.asOffsetAtEndOfDay())
        bioWebClient.getExtSubmissionsAsSequence(query).forEach(::notify)
    }

    private fun notify(extSubmission: ExtSubmission) {
        logger.info { "Notifying submission release for ${extSubmission.accNo}" }
        eventsPublisherService.submissionReleased(extSubmission)
    }
}
