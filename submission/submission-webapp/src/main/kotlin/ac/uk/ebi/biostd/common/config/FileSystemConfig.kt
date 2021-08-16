package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import uk.ac.ebi.fire.client.integration.web.FireWebClient

@Configuration
@Import(value = [WebConfig::class, FilesHandlerConfig::class])
class FileSystemConfig(
    private val folderResolver: SubmissionFolderResolver,
    private val fireWebClient: FireWebClient
) {
//    @Bean
//    fun nfsFilePersistenceService(): FilesService = NfsFilesService(folderResolver)

    @Bean
//    @ConditionalOnProperty(prefix = "app.persistence", name = ["enableFire"], havingValue = "true")
    fun fireFileService(): FilesService = FireFilesService(fireWebClient)
}