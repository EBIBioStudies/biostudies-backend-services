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
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabUtil
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ac.uk.ebi.biostd.persistence.integration.config.SqlPersistenceConfig
import ebi.ac.uk.extended.mapping.to.ToFileList
import ebi.ac.uk.extended.mapping.to.ToFilesTable
import ebi.ac.uk.extended.mapping.to.ToSection
import ebi.ac.uk.extended.mapping.to.ToSubmission
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.io.File
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.fire.client.integration.web.FireWebClient

@Configuration
@Import(value = [SqlPersistenceConfig::class, FileSystemConfig::class])
class PersistenceConfig(
    private val folderResolver: SubmissionFolderResolver,
    private val properties: ApplicationProperties,
    private val serializationService: SerializationService,
    private val submissionQueryService: SubmissionQueryService,
    private val fireWebClient: FireWebClient
) {
    @Bean
    @ConditionalOnProperty(prefix = "app.persistence",
        name = ["enableFire"],
        havingValue = "false",
        matchIfMissing = true)
    fun nfsFtpService(): FtpService = NfsFtpService(folderResolver, submissionQueryService)

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence",
        name = ["enableFire"],
        havingValue = "false",
        matchIfMissing = true)
    fun nfsPageTabService(pageTabUtil: PageTabUtil): PageTabService =
        NfsPageTabService(folderResolver, serializationService, pageTabUtil)

    @Bean
    fun pageTabUtil(): PageTabUtil =
        PageTabUtil(ToSubmission(ToSection(ToFileList())), ToFilesTable(ToFileList()))


    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enableFire"], havingValue = "true")
    fun fireFtpService(): FtpService = FireFtpService(fireWebClient, submissionQueryService)

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enableFire"], havingValue = "true")
    fun firePageTabService(pageTabUtil: PageTabUtil): PageTabService =
        FirePageTabService(File(properties.fireTempDirPath), serializationService, fireWebClient, pageTabUtil)

    @Bean
    fun fileSystemService(
        ftpService: FtpService,
        filesService: FilesService,
        pageTabService: PageTabService
    ): FileSystemService = FileSystemService(ftpService, filesService, pageTabService)
}
