package uk.ac.ebi.scheduler.releaser.service

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.util.date.asOffsetAtEndOfDay
import ebi.ac.uk.util.date.asOffsetAtStartOfDay
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.scheduler.releaser.config.NotificationTimes
import java.time.LocalDate
import java.time.OffsetDateTime

class SubmissionReleaserService(
    private val bioWebClient: BioWebClient,
    private val notificationTimes: NotificationTimes,
    private val eventsPublisherService: EventsPublisherService
) {
    fun notifySubmissionReleases() {
        val today = LocalDate.now()
        notifyRelease(today.plusDays(notificationTimes.firstWarning))
        notifyRelease(today.plusDays(notificationTimes.secondWarning))
        notifyRelease(today.plusDays(notificationTimes.thirdWarning))
    }

    fun releaseDailySubmissions() {
        val today = LocalDate.now()
        processExtPages(
            today.asOffsetAtStartOfDay(),
            today.asOffsetAtEndOfDay()
        ) {
            if (it.released.not()) bioWebClient.submitExt(it.copy(released = true))
        }
    }

    private fun notifyRelease(date: LocalDate) {
        processExtPages(
            date.asOffsetAtStartOfDay(),
            date.asOffsetAtEndOfDay()
        ) {
            eventsPublisherService.submissionReleased(it)
        }
    }

    private fun processExtPages(from: OffsetDateTime, to: OffsetDateTime, processingFunction: (ExtSubmission) -> Unit) {
        var currentPage = bioWebClient.getExtSubmissions(fromRTime = from, toRTime = to)
        currentPage.content.forEach(processingFunction)

        while (currentPage.next.isNotBlank()) {
            currentPage = bioWebClient.getExtSubmissionsPage(currentPage.next!!)
            currentPage.content.forEach(processingFunction)
        }
    }
}
