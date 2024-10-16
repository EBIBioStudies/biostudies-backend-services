package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.dto.AcceptedSubmission
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument
import ac.uk.ebi.pmc.persistence.domain.ErrorsService
import ac.uk.ebi.pmc.persistence.domain.SubmissionService
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.extended.model.StorageMode
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.ExperimentalTime

private val logger = KotlinLogging.logger {}
private const val CONCURRENCY = 20

@OptIn(ExperimentalTime::class)
class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val errorService: ErrorsService,
    private val submissionService: SubmissionService,
) {
    fun submitAll(
        sourceFile: String?,
        limit: Int?,
    ): Unit =
        runBlocking {
            submitSubmissions(sourceFile, limit)
        }

    fun submitSingle(submissionId: String): Unit =
        runBlocking {
            val submission = submissionService.findById(submissionId)
            submitSubmission(submission, 1)
        }

    private suspend fun submitSubmissions(
        sourceFile: String?,
        limit: Int?,
    ) {
        val counter = AtomicInteger(0)
        supervisorScope {
            submissionService
                .findReadyToSubmit(sourceFile)
                .take(limit ?: Int.MAX_VALUE)
                .concurrently(CONCURRENCY) { submitSubmission(it, counter.incrementAndGet()) }
                .collect()
        }
    }

    private suspend fun submitSubmission(
        sub: SubmissionDocument,
        idx: Int,
    ): Unit =
        submit(sub)
            .fold(
                {
                    logger.info { "submitted $idx, accNo='${sub.accNo}', version='${sub.version}'" }
                    submissionService.saveSubmittingSubmission(sub, it.version)
                },
                {
                    logger.error(it) { "failed to submit accNo='${sub.accNo}'" }
                    errorService.saveError(sub, PmcMode.SUBMIT, it)
                },
            )

    private suspend fun submit(submission: SubmissionDocument): Result<AcceptedSubmission> {
        suspend fun submit(): AcceptedSubmission {
            val files = submissionService.getSubFiles(submission.files).map { File(it.path) }.toList()
            val params = SubmitParameters(storageMode = StorageMode.NFS, silentMode = true, singleJobMode = true)
            return bioWebClient.submitMultipartAsync(submission.body, SubmissionFormat.JSON, params, files)
        }

        return runCatching { submit() }
    }
}
