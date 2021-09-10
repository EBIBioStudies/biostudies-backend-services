package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.FirePageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.NfsPageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ac.uk.ebi.biostd.persistence.integration.config.SqlPersistenceConfig
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.io.File

@Configuration
@Import(value = [SqlPersistenceConfig::class, FileSystemConfig::class])
class PersistenceConfig(
    private val folderResolver: SubmissionFolderResolver,
    private val applicationProperties: ApplicationProperties,
    private val serializationService: SerializationService,
    private val submissionQueryService: SubmissionQueryService,
    private val fireWebClient: FireWebClient
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "app.persistence",
        name = ["enableFire"],
        havingValue = "false",
        matchIfMissing = true
    )
    fun nfsFtpService(): FtpService = NfsFtpService(folderResolver, submissionQueryService)

    @Bean
    @ConditionalOnProperty(
        prefix = "app.persistence",
        name = ["enableFire"],
        havingValue = "false",
        matchIfMissing = true
    )
    fun nfsPageTabService(): PageTabService = NfsPageTabService(folderResolver, serializationService)

    @Bean
    fun fireFtpService(): FtpService = FireFtpService(fireWebClient, submissionQueryService)

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enableFire"], havingValue = "true")
    fun firePageTabService(): PageTabService =
        FirePageTabService(File(applicationProperties.fireTempDirPath), serializationService, fireWebClient)

    @Bean
    fun fileSystemService(
        ftpService: FtpService,
        filesService: FilesService,
        pageTabService: PageTabService
    ): FileSystemService = FileSystemService(ftpService, filesService, pageTabService)
}
