package ac.uk.ebi.biostd.submission.stats

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.DIRECTORIES
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.NON_DECLARED_FILES_DIRECTORIES
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.PersistedExtFile
import ebi.ac.uk.paths.SubmissionFolderResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow

private val logger = KotlinLogging.logger {}

class SubmissionStatsCalculator(
    private val serializationService: ExtSerializationService,
    private val fileStorageService: FileStorageService,
    private val subFolderResolver: SubmissionFolderResolver,
    private val pageTabService: PageTabService,
) {
    internal suspend fun calculateStats(sub: ExtSubmission): List<SubmissionStat> {
        copyPageTabFiles(sub)
        return calculatePlainStats(sub)
    }

    private suspend fun calculatePlainStats(sub: ExtSubmission): List<SubmissionStat> {
        logger.info { "Calculating stats for submission ${sub.accNo}, version ${sub.version}" }
        var subFilesSize = 0L
        var directories = mutableListOf<String>()

        serializationService
            .filesFlow(sub)
            .filterIsInstance<PersistedExtFile>()
            .collect {
                if (it.type == ExtFileType.FILE) subFilesSize += it.size
                if (it.type == ExtFileType.DIR) directories.add(it.filePath.removeSuffix(".zip"))
            }

        val emptyDirectories = directories.count { hasFiles(it, sub) }

        return listOf(
            SubmissionStat(sub.accNo, subFilesSize, FILES_SIZE),
            SubmissionStat(sub.accNo, directories.size.toLong(), DIRECTORIES),
            SubmissionStat(sub.accNo, emptyDirectories.toLong(), NON_DECLARED_FILES_DIRECTORIES),
        )
    }

    private suspend fun copyPageTabFiles(sub: ExtSubmission): List<ExtFile> {
        logger.info { "Copying pagetab files for submission ${sub.accNo}, version ${sub.version}" }
        return with(Dispatchers.IO) {
            val expectedPath = subFolderResolver.getCopyPageTabPath(sub)
            val tabFiles = pageTabService.generatePageTab(sub, false)
            tabFiles.onEach { fileStorageService.copyFile(it, expectedPath) }
        }
    }

    private suspend fun hasFiles(
        directoryPath: String,
        sub: ExtSubmission,
    ): Boolean =
        serializationService
            .filesFlow(sub)
            .filterIsInstance<PersistedExtFile>()
            .filter { it.type == ExtFileType.FILE }
            .firstOrNull { it.filePath.contains(directoryPath) } != null
}
