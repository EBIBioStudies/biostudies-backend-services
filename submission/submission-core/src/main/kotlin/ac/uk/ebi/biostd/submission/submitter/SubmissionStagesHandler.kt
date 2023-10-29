package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
import ebi.ac.uk.extended.events.RequestCheckedReleased
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestFilesCopied
import ebi.ac.uk.extended.events.RequestFinalized
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.events.RequestMessage
import ebi.ac.uk.extended.events.RequestPersisted
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService

private val logger = KotlinLogging.logger {}

class SubmissionStagesHandler(
    private val statsService: SubmissionStatsService,
    private val submissionSubmitter: SubmissionSubmitter,
    private val eventsPublisherService: EventsPublisherService,
) {
    fun indexRequest(rqt: RequestCreated) {
        processSafely(rqt) {
            submissionSubmitter.indexRequest(rqt)
            eventsPublisherService.requestIndexed(rqt.accNo, rqt.version)
        }
    }

    fun loadRequest(rqt: RequestIndexed) {
        processSafely(rqt) {
            submissionSubmitter.loadRequest(rqt)
            eventsPublisherService.requestLoaded(rqt.accNo, rqt.version)
        }
    }

    fun cleanRequest(rqt: RequestLoaded) {
        processSafely(rqt) {
            submissionSubmitter.cleanRequest(rqt)
            eventsPublisherService.requestCleaned(rqt.accNo, rqt.version)
        }
    }

    fun copyRequestFiles(rqt: RequestCleaned) {
        processSafely(rqt) {
            submissionSubmitter.processRequest(rqt)
            eventsPublisherService.requestFilesCopied(rqt.accNo, rqt.version)
        }
    }

    fun checkReleased(rqt: RequestFilesCopied) {
        processSafely(rqt) {
            submissionSubmitter.checkReleased(rqt)
            eventsPublisherService.checkReleased(rqt.accNo, rqt.version)
        }
    }

    fun saveSubmission(rqt: RequestCheckedReleased) {
        processSafely(rqt) {
            submissionSubmitter.saveRequest(rqt)
            eventsPublisherService.submissionPersisted(rqt.accNo, rqt.version)
        }
    }

    fun finalizeRequest(rqt: RequestPersisted) {
        processSafely(rqt) {
            submissionSubmitter.finalizeRequest(rqt)
        }
    }

    fun calculateStats(rqt: RequestFinalized) {
        processSafely(rqt) {
            statsService.calculateSubFilesSize(accNo)
        }
    }

    private fun processSafely(request: RequestMessage, process: suspend RequestMessage.() -> Unit) = runBlocking {
        runCatching { process(request) }.onFailure { onError(it, request) }
    }

    private fun onError(exception: Throwable, rqt: RequestMessage) {
        logger.error(exception) { "${rqt.accNo}, Problem processing request '${rqt.accNo}': ${exception.message}" }
        eventsPublisherService.submissionFailed(rqt)
    }
}
