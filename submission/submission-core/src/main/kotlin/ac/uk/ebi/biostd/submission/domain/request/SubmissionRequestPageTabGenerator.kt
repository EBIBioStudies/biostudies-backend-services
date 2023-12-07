package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class SubmissionRequestPageTabGenerator(
    private val pageTabService: PageTabService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    suspend fun generatePageTab(accNo: String, version: Int, processId: String) {
        val (changeId, request) = requestService.getSubmissionRequest(accNo, version, LOADED, processId)
        logger.info { "$accNo ${request.submission.owner} Started generating pagetab files" }

        val withTabFiles = pageTabService.generatePageTab(request.submission)
        val index = AtomicInteger(request.totalFiles)

        withTabFiles
            .allPageTabFiles
            .filterIsInstance<NfsFile>()
            .map { it.copy(md5 = it.file.md5(), size = it.file.size()) }
            .map { SubmissionRequestFile(accNo, version, index.incrementAndGet(), it.filePath, it) }
            .forEach { filesRequestService.saveSubmissionRequestFile(it) }

        logger.info { "$accNo ${withTabFiles.owner} Finished generating pagetab files" }

        requestService.saveRequest(request.withPageTab(submission = withTabFiles, totalFiles = index.get(), changeId))
    }
}
