package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ac.uk.ebi.biostd.persistence.integration.config.SqlPersistenceConfig
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(value = [SqlPersistenceConfig::class, FileSystemConfig::class])
class PersistenceConfig(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService,
    private val submissionQueryService: SubmissionQueryService
) {
    @Bean
    fun ftpFilesService(): FtpService = NfsFtpService(folderResolver, submissionQueryService)

    @Bean
    fun pageTabService(): PageTabService = PageTabService(folderResolver, serializationService)

    @Bean
    fun fileSystemService(
        ftpService: FtpService,
        filesService: FilesService,
        pageTabService: PageTabService
    ): FileSystemService = FileSystemService(ftpService, filesService, pageTabService)
}
