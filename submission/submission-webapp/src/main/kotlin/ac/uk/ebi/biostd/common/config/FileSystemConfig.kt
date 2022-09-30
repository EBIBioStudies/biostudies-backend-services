package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.config.SubmitterConfig.FilesHandlerConfig
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File

// TODO delete
@Configuration
@Import(value = [WebConfig::class, FilesHandlerConfig::class])
class FileSystemConfig(
//    private val folderResolver: SubmissionFolderResolver,
    private val applicationProperties: ApplicationProperties,
) {
//    @Bean
//    fun nfsFileService(): NfsFilesService = NfsFilesService()

//    @Bean
//    fun fireFileService(
//        fireService: FireService,
//        fileProcessingService: FileProcessingService,
//        serializationService: ExtSerializationService,
//    ): FireFilesService = FireFilesService(fireService, fileProcessingService, serializationService)

//    @Bean
//    fun extFilesResolver() = FilesResolver(File(applicationProperties.requestFilesPath))
}
