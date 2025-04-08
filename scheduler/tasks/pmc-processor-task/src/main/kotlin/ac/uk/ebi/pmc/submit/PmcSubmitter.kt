package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument
import ac.uk.ebi.pmc.persistence.domain.ErrorsService
import ac.uk.ebi.pmc.persistence.domain.SubmissionService
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.coroutines.chunked
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.model.SubmissionId
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import mu.KotlinLogging
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.ExperimentalTime

private val logger = KotlinLogging.logger {}
private const val CONCURRENCY = 20
private const val BATCH_SIZE = 200

@OptIn(ExperimentalTime::class)
class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val errorService: ErrorsService,
    private val submissionService: SubmissionService,
) {
    fun submitAll(
        sourceFile: String?,
        limit: Int?,
        batchSize: Int?,
    ): Unit =
        runBlocking {
            submitSubmissions(sourceFile, limit, batchSize)
        }

    fun submitSingle(submissionId: String): Unit =
        runBlocking {
            val submission = submissionService.findById(submissionId)
            submitMany(listOf(submission), 1)
        }

    private suspend fun submitSubmissions(
        sourceFile: String?,
        limit: Int?,
        configuredBatchSize: Int?,
    ) {
        val counter = AtomicInteger(0)
        val batchSize = configuredBatchSize ?: BATCH_SIZE

        logger.info { "Submitting submission sourceFile='$sourceFile', batchSize='$batchSize', limit='$limit'" }

        supervisorScope {
            submissionService
                .findReadyToSubmit(sourceFile, limit ?: Int.MAX_VALUE)
                .chunked(batchSize)
                .concurrently(CONCURRENCY) { submitMany(it, counter.getAndIncrement()) }
                .collect()
        }
    }

    private suspend fun submitMany(
        sub: List<SubmissionDocument>,
        idx: Int,
    ) {
        logger.info { "Submitting ${sub.size} submissions. Batch $idx" }
        submitMany(sub)
            .fold(
                {
                    logger.info { "submitted batch $idx, accNos='${sub.map { it.accNo }.joinToString()}'" }
                    submissionService.saveSubmittingSubmissions(sub, it)
                },
                {
                    logger.error(it) { "failed to batch $idx, accNos='${sub.map { it.accNo }.joinToString()}'" }
                    errorService.saveErrors(sub, PmcMode.SUBMIT, it)
                },
            )
    }

    private suspend fun submitMany(submissions: List<SubmissionDocument>): Result<List<SubmissionId>> {
        suspend fun getSubFiles(sub: SubmissionDocument): List<File> =
            submissionService.getSubFiles(sub.files).map { File(it.path) }.toList()

        suspend fun submitMany(): List<SubmissionId> {
            val submissionsMap = submissions.associateBy({ it.accNo }, { it.body })
            val params = SubmitParameters(storageMode = StorageMode.NFS, silentMode = true, singleJobMode = true)
            val files = submissions.map { it.accNo to getSubFiles(it) }.toMap()

            return bioWebClient.submitMultipartAsync(
                submissions = submissionsMap,
                parameters = params,
                format = "json",
                files = files,
            )
        }

        return runCatching { submitMany() }
    }
}
