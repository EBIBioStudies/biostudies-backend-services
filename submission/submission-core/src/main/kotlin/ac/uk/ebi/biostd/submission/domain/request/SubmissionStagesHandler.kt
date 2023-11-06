package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionSubmitter
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
    private val submissionSubmitter: ExtSubmissionSubmitter,
    private val eventsPublisherService: EventsPublisherService,
) {
    fun indexRequest(rqt: RequestCreated) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            submissionSubmitter.indexRequest(accNo, version)
            eventsPublisherService.requestIndexed(accNo, version)
        }
    }

    fun loadRequest(rqt: RequestIndexed) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            submissionSubmitter.loadRequest(accNo, version)
            eventsPublisherService.requestLoaded(accNo, version)
        }
    }

    fun cleanRequest(rqt: RequestLoaded) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            submissionSubmitter.cleanRequest(accNo, version)
            eventsPublisherService.requestCleaned(accNo, version)
        }
    }

    fun copyRequestFiles(rqt: RequestCleaned) {
        processSafely(rqt) {
            submissionSubmitter.processRequest(accNo, version)
            eventsPublisherService.requestFilesCopied(accNo, version)
        }
    }

    fun checkReleased(rqt: RequestFilesCopied) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            submissionSubmitter.checkReleased(accNo, version)
            eventsPublisherService.checkReleased(accNo, version)
        }
    }

    fun saveSubmission(rqt: RequestCheckedReleased) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            submissionSubmitter.saveRequest(accNo, version)
            eventsPublisherService.submissionPersisted(accNo, version)
        }
    }

    fun finalizeRequest(rqt: RequestPersisted) {
        processSafely(rqt) {
            val (accNo, version) = rqt
            submissionSubmitter.finalizeRequest(accNo, version)
        }
    }

    fun calculateStats(rqt: RequestFinalized) {
        processSafely(rqt) {
            statsService.calculateSubFilesSize(rqt.accNo)
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
