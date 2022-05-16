package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File

@Configuration
@Import(value = [WebConfig::class, FilesHandlerConfig::class])
class FileSystemConfig(
    private val folderResolver: SubmissionFolderResolver,
    private val applicationProperties: ApplicationProperties
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "app.persistence",
        name = ["enableFire"],
        havingValue = "false",
        matchIfMissing = true
    )
    fun nfsFilePersistenceService(fileProcessingService: FileProcessingService): FilesService =
        NfsFilesService(folderResolver, fileProcessingService)

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enableFire"], havingValue = "true")
    fun fireFileService(fireService: FireService, fileProcessingService: FileProcessingService): FilesService =
        FireFilesService(fireService, fileProcessingService)

    @Bean
    fun extFilesResolver() = FilesResolver(File(applicationProperties.requestFilesPath))
}
