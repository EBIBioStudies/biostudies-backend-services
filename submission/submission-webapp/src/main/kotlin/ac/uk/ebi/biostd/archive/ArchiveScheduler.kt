package ac.uk.ebi.biostd.archive

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.io.FileUtils
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import java.nio.file.Path
import java.nio.file.Paths
import ebi.ac.uk.system.tempFolder as systemTempFolder

private val logger = KotlinLogging.logger {}

class ArchiveScheduler(
    private val props: ApplicationProperties,
    private val persistenceService: SubmissionRequestPersistenceService,
) {
    /**
     * Delete request files every day at 1 am.
     */
    @Scheduled(cron = "0 0 1 * * *")
    fun deleteRequestFiles() =
        runBlocking {
            persistenceService
                .findAllProcessed()
                .onEach { (accNo, version) -> logger.info { "Deleting request files $accNo, $version" } }
                .collect { (accNo, version) ->
                    val subTempFolder = Paths.get(props.fire.tempDirPath).resolve("$accNo/$version").toFile()
                    FileUtils.deleteFile(subTempFolder)
                    logger.info { "Deleted request files for $accNo, $version" }
                }
        }

    /**
     * Archive request every day at 4 am.
     */
    @Scheduled(cron = "0 0 4 * * *")
    fun archiveRequests() =
        runBlocking {
            persistenceService
                .findAllProcessed()
                .onEach { (accNo, version) -> logger.info { "Archiving request $accNo, $version" } }
                .collect { (accNo, version) ->
                    persistenceService.archiveRequest(accNo, version)
                    logger.info { "Archived request $accNo, $version" }
                }
        }

    /**
     * Clean files in temp folders every week, on sundays, at 5 am
     */
    @Scheduled(cron = "0 0 5 * * 0")
    fun cleanTempFolders() {
        FileUtils.cleanDirectory(Path.of(props.persistence.tempDirPath).toFile())
        FileUtils.cleanDirectory(systemTempFolder().toFile())
    }
}
