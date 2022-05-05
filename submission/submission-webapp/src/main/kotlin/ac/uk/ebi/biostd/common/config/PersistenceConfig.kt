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
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ac.uk.ebi.biostd.persistence.integration.config.SqlPersistenceConfig
import ebi.ac.uk.extended.mapping.to.ToFilesTableMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtFilesResolver
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.io.File

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
    fun nfsPageTabService(pageTabUtil: PageTabUtil, fileProcessingService: FileProcessingService): PageTabService =
        NfsPageTabService(folderResolver, serializationService, pageTabUtil, fileProcessingService)

    @Bean
    fun pageTabUtil(toSubmissionMapper: ToSubmissionMapper, toFilesTableMapper: ToFilesTableMapper): PageTabUtil =
        PageTabUtil(toSubmissionMapper, toFilesTableMapper)

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enableFire"], havingValue = "true")
    fun fireFtpService(serializationService: ExtSerializationService): FtpService =
        FireFtpService(fireWebClient, serializationService, submissionQueryService)

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enableFire"], havingValue = "true")
    fun firePageTabService(pageTabUtil: PageTabUtil, fileProcessingService: FileProcessingService): PageTabService =
        FirePageTabService(
            File(properties.fireTempDirPath),
            serializationService,
            fireWebClient,
            pageTabUtil,
            fileProcessingService
        )

    @Bean
    fun fileSystemService(
        ftpService: FtpService,
        filesService: FilesService,
        pageTabService: PageTabService
    ): FileSystemService = FileSystemService(ftpService, filesService, pageTabService)

    @Bean
    fun fileProcessingService(serializationService: ExtSerializationService, fileResolver: ExtFilesResolver) =
        FileProcessingService(serializationService, fileResolver)
}
