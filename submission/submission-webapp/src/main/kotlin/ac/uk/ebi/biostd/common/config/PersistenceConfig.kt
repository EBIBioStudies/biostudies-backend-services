package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.filesystem.FileSystemService
import ac.uk.ebi.biostd.persistence.common.filesystem.FilesService
import ac.uk.ebi.biostd.persistence.common.filesystem.FtpFilesService
import ac.uk.ebi.biostd.persistence.integration.config.SqlPersistenceConfig
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(value = [SqlPersistenceConfig::class])
class PersistenceConfig(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {
    @Bean
    fun ftpFilesService() = FtpFilesService(folderResolver)

    @Bean
    fun filePersistenceService() = FilesService(folderResolver, serializationService)

    @Bean
    fun fileSystemService(
        filesService: FilesService,
        ftpService: FtpFilesService
    ) = FileSystemService(filesService, ftpService)
}
