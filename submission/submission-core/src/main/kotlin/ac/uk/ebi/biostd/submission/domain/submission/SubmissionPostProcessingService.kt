package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.paths.SubmissionFolderResolver
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionPostProcessingService(
    private val fileStorageService: FileStorageService,
    private val subFolderResolver: SubmissionFolderResolver,
    private val submissionStatsService: SubmissionStatsService,
    private val extSubQueryService: SubmissionPersistenceQueryService,
) {
    suspend fun postProcess(accNo: String) {
        submissionStatsService.calculateStats(accNo)
        copyPageTabFiles(accNo)
    }

    private suspend fun copyPageTabFiles(accNo: String): List<ExtFile> {
        val sub = extSubQueryService.getExtByAccNo(accNo, includeFileListFiles = false, includeLinkListLinks = false)

        logger.info { "Started copying pagetab files for submission ${sub.accNo}, version ${sub.version}" }
        val pageTabFiles =
            with(Dispatchers.IO) {
                val expectedPath = subFolderResolver.getCopyPageTabPath(sub)
                sub.allPageTabFiles.onEach { fileStorageService.copyFile(it, expectedPath) }
            }
        logger.info { "Finished copying pagetab files for submission ${sub.accNo}, version ${sub.version}" }

        return pageTabFiles
    }
}
