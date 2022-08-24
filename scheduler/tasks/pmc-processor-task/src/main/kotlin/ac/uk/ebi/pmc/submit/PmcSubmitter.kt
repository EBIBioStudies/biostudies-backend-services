package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.extended.model.StorageMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}
private const val BUFFER_SIZE = 30

class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val errorDocService: ErrorsDocService,
    private val submissionService: SubmissionDocService,
) {

    fun submitAll() = runBlocking {
        submitSubmissions()
    }

    private suspend fun submitSubmissions() = withContext(Dispatchers.Default) {
        submissionService.findReadyToSubmit()
            .map { async { submitSubmission(it) } }
            .buffer(BUFFER_SIZE)
            .collect { it.await() }
    }

    private suspend fun submitSubmission(submission: SubmissionDoc) = coroutineScope {
        runCatching {
            logger.info { "submitting accNo='${submission.accNo}'" }
            val files = submissionService.getSubFiles(submission.files).map { File(it.path) }
            val filesConfig = SubmissionFilesConfig(files)
            bioWebClient.submitSingle(submission.body, SubmissionFormat.JSON, StorageMode.NFS, filesConfig)
        }.fold(
            { submissionService.changeStatus(submission, SubmissionStatus.SUBMITTED) },
            { errorDocService.saveError(submission, PmcMode.SUBMIT, it) }
        )
    }
}
