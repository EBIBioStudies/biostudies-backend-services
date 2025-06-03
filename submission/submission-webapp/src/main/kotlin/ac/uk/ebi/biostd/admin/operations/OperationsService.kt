package ac.uk.ebi.biostd.admin.operations

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.stats.web.TempFileGenerator
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.system.tempFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

class OperationsService(
    private val props: ApplicationProperties,
    private val persistenceService: SubmissionRequestPersistenceService,
    private val tempFileGenerator: TempFileGenerator,
) {
    suspend fun deleteRequestFiles() {
        persistenceService
            .findAllProcessed()
            .onEach { (accNo, version) -> logger.info { "Deleting request files $accNo, $version" } }
            .collect { (accNo, version) ->
                runSafely("Deleting temp Files accNo='$accNo', version:'$version'") {
                    deleteRequestFiles(accNo, version)
                    logger.info { "Deleted request files for $accNo, $version" }
                }
            }
    }

    suspend fun deleteRequestFiles(
        accNo: String,
        version: Int,
    ) = withContext(Dispatchers.IO) {
        val subPath = "$accNo/$version"
        val fireTempFolder = Paths.get(props.fire.tempDirPath).resolve(subPath).toFile()
        val subTempFolder = Paths.get(props.persistence.requestFilesPath).resolve(subPath).toFile()

        FileUtils.deleteFile(subTempFolder)
        FileUtils.deleteFile(fireTempFolder)
        logger.info { "Deleted request files for $accNo, $version" }
    }

    suspend fun archiveRequests() =
        persistenceService
            .findAllProcessed()
            .onEach { (accNo, version) -> logger.info { "Archiving request $accNo, $version" } }
            .concurrently(ARCHIVE_CONCURRENCY) { (accNo, version) ->
                runSafely("Archive accNo='$accNo', version:'$version'") {
                    persistenceService.archiveRequest(accNo, version)
                    logger.info { "Archived request $accNo, $version" }
                }
            }.collect()

    suspend fun archiveRequests(
        accNo: String,
        version: Int,
    ) = runBlocking {
        runSafely("Archive accNo='$accNo', version:'$version'") {
            persistenceService.archiveRequest(accNo, version)
            logger.info { "Archived request $accNo, $version" }
        }
    }

    private suspend fun runSafely(
        description: String,
        function: suspend () -> Unit,
    ) {
        runCatching { function() }.onFailure { logger.error(description, it) }
    }

    suspend fun cleanTempFolders() {
        withContext(Dispatchers.IO) {
            tempFileGenerator.deleteOldFiles(FILE_MONTHS_DELETE_THREADHOLD)
            FileUtils.cleanDirectory(tempFolder().toFile())
        }
    }

    companion object {
        const val FILE_MONTHS_DELETE_THREADHOLD = 3L
        const val ARCHIVE_CONCURRENCY = 30
    }
}
