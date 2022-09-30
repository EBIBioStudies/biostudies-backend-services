package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabUtil
import ac.uk.ebi.biostd.persistence.integration.config.SqlPersistenceConfig
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File

@Configuration
@Import(value = [SqlPersistenceConfig::class])
class FilePersistenceConfig(
    private val fireClient: FireClient,
    private val properties: ApplicationProperties,
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService,
) {
    @Bean
    fun nfsFileService(): NfsFilesService = NfsFilesService(folderResolver)

    @Bean
    fun nfsFtpService(): NfsFtpService = NfsFtpService(folderResolver)

    @Bean
    fun fireFtpService(): FireFtpService = FireFtpService(fireClient)

    @Bean
    fun fireFilesService(): FireFilesService = FireFilesService(fireClient, File(properties.fireTempDirPath))

    @Bean
    fun pageTabService(
        pageTabUtil: PageTabUtil,
    ): PageTabService = PageTabService(File(properties.fireTempDirPath), pageTabUtil)

    @Bean
    fun pageTabUtil(
        toFileListMapper: ToFileListMapper,
        toSubmissionMapper: ToSubmissionMapper,
    ): PageTabUtil = PageTabUtil(serializationService, toSubmissionMapper, toFileListMapper)

    @Bean
    fun extFilesResolver() = FilesResolver(File(properties.requestFilesPath))
}
