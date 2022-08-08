package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.client.integration.web.SubmissionResponse
import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.scheduler.properties.PmcMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.ExperimentalTime
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}
private const val BUFFER_SIZE = 20
private const val TIMEOUT = 25_000L

class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val errorDocService: ErrorsDocService,
    private val submissionService: SubmissionDocService,
) {

    fun submitAll() = runBlocking {
        submitSubmissions()
    }

    private suspend fun submitSubmissions() = coroutineScope {
        val counter = AtomicInteger(0)
        submissionService.findReadyToSubmit()
            .map { withTimeout(TIMEOUT) { async(Dispatchers.IO) { submitSubmission(it, counter.incrementAndGet()) } } }
            .buffer(BUFFER_SIZE)
            .map { it.await() }
            .collect()
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun submitSubmission(sub: SubmissionDoc, idx: Int) = coroutineScope {
        runCatching { submit(sub) }
            .fold(
                {
                    logger.info { "submitted $idx, accNo='${sub.accNo}', in ${it.duration.inWholeMilliseconds} ms" }
                    submissionService.changeStatus(sub, SubmissionStatus.SUBMITTED)
                },
                {
                    logger.error(it) { "failed to submit accNo='${sub.accNo}'" }
                    errorDocService.saveError(sub, PmcMode.SUBMIT, it)
                }
            )
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun submit(submission: SubmissionDoc): TimedValue<SubmissionResponse> {
        return measureTimedValue {
            val files = submissionService.getSubFiles(submission.files).map { File(it.path) }
            val filesConfig = SubmissionFilesConfig(files)
            bioWebClient.submitSingle(submission.body, SubmissionFormat.JSON, filesConfig)
        }
    }
}
