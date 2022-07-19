package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.TsvPagetabExtension
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.FirePageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.NfsPageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabUtil
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ac.uk.ebi.biostd.persistence.integration.config.SqlPersistenceConfig
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File

@Configuration
@Import(value = [SqlPersistenceConfig::class, FileSystemConfig::class])
class PersistenceConfig(
    private val folderResolver: SubmissionFolderResolver,
    private val properties: ApplicationProperties,
    private val serializationService: SerializationService,
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
    private val fireClient: FireClient,
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "app.persistence",
        name = ["enableFire"],
        havingValue = "false",
        matchIfMissing = true
    )
    fun nfsFtpService(): FtpService = NfsFtpService(folderResolver, submissionPersistenceQueryService)

    @Bean
    @ConditionalOnProperty(
        prefix = "app.persistence",
        name = ["enableFire"],
        havingValue = "false",
        matchIfMissing = true
    )
    fun nfsPageTabService(
        pageTabUtil: PageTabUtil,
        fileProcessingService: FileProcessingService,
        tsvPagetabExtension: TsvPagetabExtension,
    ): PageTabService =
        NfsPageTabService(folderResolver, pageTabUtil, fileProcessingService, tsvPagetabExtension)

    @Bean
    fun pageTabUtil(
        toSubmissionMapper: ToSubmissionMapper,
        toFileListMapper: ToFileListMapper,
        tsvPagetabExtension: TsvPagetabExtension,
    ): PageTabUtil = PageTabUtil(serializationService, toSubmissionMapper, toFileListMapper, tsvPagetabExtension)

    @Bean
    fun tsvPagetabExtension(): TsvPagetabExtension = TsvPagetabExtension(properties.featureFlags.tsvPagetabExtension)

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enableFire"], havingValue = "true")
    fun fireFtpService(serializationService: ExtSerializationService): FtpService =
        FireFtpService(fireClient, serializationService, submissionPersistenceQueryService)

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enableFire"], havingValue = "true")
    fun firePageTabService(
        pageTabUtil: PageTabUtil,
        fileProcessingService: FileProcessingService,
        tsvPagetabExtension: TsvPagetabExtension,
    ): PageTabService =
        FirePageTabService(
            File(properties.fireTempDirPath),
            fireClient,
            pageTabUtil,
            fileProcessingService,
            tsvPagetabExtension
        )

    @Bean
    @ConditionalOnProperty(prefix = "app.persistence", name = ["enableFire"], havingValue = "true")
    fun fireService(): FireService = FireService(fireClient, File(properties.fireTempDirPath))

    @Bean
    fun fileSystemService(
        ftpService: FtpService,
        filesService: FilesService,
        pageTabService: PageTabService,
    ): FileSystemService = FileSystemService(filesService, pageTabService)
}
