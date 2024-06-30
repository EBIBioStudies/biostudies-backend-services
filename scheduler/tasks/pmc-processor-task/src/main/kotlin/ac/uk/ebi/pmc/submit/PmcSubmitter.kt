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
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.extended.model.StorageMode
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
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

@OptIn(ExperimentalTime::class)
class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val errorDocService: ErrorsDocService,
    private val submissionService: SubmissionDocService,
) {
    fun submitAll(sourceFile: String?): Unit =
        runBlocking {
            submitSubmissions(sourceFile)
        }

    fun submitSingle(submissionId: String): Unit =
        runBlocking {
            val submission = submissionService.findById(submissionId)
            submitSubmission(submission, 1)
        }

    private suspend fun submitSubmissions(sourceFile: String?) {
        val counter = AtomicInteger(0)
        supervisorScope {
            submissionService.findReadyToSubmit(sourceFile)
                .concurrently(BUFFER_SIZE) { submitSubmission(it, counter.incrementAndGet()) }
                .collect()
        }
    }

    private suspend fun submitSubmission(
        sub: SubmissionDoc,
        idx: Int,
    ): Unit =
        submit(sub)
            .fold(
                {
                    logger.info { "submitted $idx, accNo='${sub.accNo}', in ${it.duration.inWholeMilliseconds} ms" }
                    submissionService.changeStatus(sub, SubmissionStatus.SUBMITTED)
                },
                {
                    logger.error(it) { "failed to submit accNo='${sub.accNo}'" }
                    errorDocService.saveError(sub, PmcMode.SUBMIT, it)
                },
            )

    private suspend fun submit(submission: SubmissionDoc): Result<TimedValue<SubmissionResponse>> {
        suspend fun submit(submission: SubmissionDoc): SubmissionResponse {
            val files = submissionService.getSubFiles(submission.files).map { File(it.path) }
            val filesConfig = SubmissionFilesConfig(files, StorageMode.NFS)
            return bioWebClient.submitSingle(submission.body, SubmissionFormat.JSON, filesConfig)
        }

        return runCatching { withTimeout(TIMEOUT) { measureTimedValue { submit(submission) } } }
    }
}
