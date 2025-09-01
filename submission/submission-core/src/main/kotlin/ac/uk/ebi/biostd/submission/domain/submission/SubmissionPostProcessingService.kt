package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.DIRECTORIES
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.NON_DECLARED_FILES_DIRECTORIES
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
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
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class SubmissionPostProcessingService(
    private val pageTabService: PageTabService,
    private val statsDataService: StatsDataService,
    private val fileStorageService: FileStorageService,
    private val subFolderResolver: SubmissionFolderResolver,
    private val serializationService: ExtSerializationService,
    private val extSubQueryService: SubmissionPersistenceQueryService,
) {
    suspend fun calculateStats(accNo: String): List<SubmissionStat> {
        val sub = extSubQueryService.getExtByAccNo(accNo, includeFileListFiles = true, includeLinkListLinks = true)
        calculateStats(sub)
        return statsDataService.findByAccNo(accNo)?.stats.orEmpty()
    }

    suspend fun generateFallbackPageTabFiles(accNo: String): List<ExtFile> {
        val sub = extSubQueryService.getExtByAccNo(accNo, includeFileListFiles = false, includeLinkListLinks = false)
        return generateFallbackPageTabFiles(sub)
    }

    suspend fun postProcess(accNo: String) {
        val sub = extSubQueryService.getExtByAccNo(accNo, includeFileListFiles = true, includeLinkListLinks = true)
        generateFallbackPageTabFiles(sub)
        calculateStats(sub)
    }

    private suspend fun generateFallbackPageTabFiles(sub: ExtSubmission): List<ExtFile> {
        logger.info { "Started copying pagetab files for submission ${sub.accNo}, version ${sub.version}" }
        val copiedFiles =
            with(Dispatchers.IO) {
                val expectedPath = subFolderResolver.getCopyPageTabPath(sub)
                val tabFiles = pageTabService.generatePageTab(sub, false)
                tabFiles.onEach { fileStorageService.copyFile(it, expectedPath) }
            }
        logger.info { "Finished copying pagetab files for submission ${sub.accNo}, version ${sub.version}" }

        return copiedFiles
    }

    private suspend fun calculateStats(sub: ExtSubmission) {
        logger.info { "Started calculating stats for submission ${sub.accNo}, version ${sub.version}" }
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
        val stats =
            listOf(
                SubmissionStat(sub.accNo, subFilesSize, FILES_SIZE),
                SubmissionStat(sub.accNo, directories.size.toLong(), DIRECTORIES),
                SubmissionStat(sub.accNo, emptyDirectories.toLong(), NON_DECLARED_FILES_DIRECTORIES),
            )

        statsDataService.saveAll(sub.accNo, stats)
        logger.info { "Finished calculating stats for submission ${sub.accNo}, version ${sub.version}" }
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

    suspend fun postProcessAll() {
        val idx = AtomicInteger(0)
        statsDataService
            .findAll(Instant.now().minus(REFRESH_DAYS, ChronoUnit.DAYS))
            .onEach { accNo -> "Post processing submission ${idx.incrementAndGet()} accNo '$accNo'" }
            .collect { accNo -> postProcessSafely(accNo) }
    }

    private suspend fun postProcessSafely(accNo: String) {
        runCatching { postProcess(accNo) }
            .onFailure { logger.error(it) { "Issues post processing accNo: '$accNo'" } }
            .onSuccess { logger.info { "Finished post processing accNo: '$accNo'" } }
    }

    companion object {
        const val REFRESH_DAYS = 30L
    }
}
