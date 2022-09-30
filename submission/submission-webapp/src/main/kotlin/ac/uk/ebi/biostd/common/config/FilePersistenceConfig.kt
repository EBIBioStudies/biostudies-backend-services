package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabUtil
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ac.uk.ebi.biostd.persistence.filesystem.service.FireStorageService
import ac.uk.ebi.biostd.persistence.filesystem.service.NfsStorageService
import ac.uk.ebi.biostd.persistence.integration.config.SqlPersistenceConfig
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File

@Configuration
@Import(value = [SqlPersistenceConfig::class, FileSystemConfig::class])
class FilePersistenceConfig(
    private val folderResolver: SubmissionFolderResolver,
    private val properties: ApplicationProperties,
    private val serializationService: SerializationService,
    private val fireClient: FireClient,
) {
    @Bean
    fun fireStorageService(
        ftpService: FireFtpService,
        filesService: FireFilesService,
        fileProcessingService: FileProcessingService,
        serializationService: ExtSerializationService,
    ): FireStorageService = FireStorageService(ftpService, filesService, fileProcessingService, serializationService)

    @Bean
    fun nfsStorageService(
        ftpService: NfsFtpService,
        filesService: NfsFilesService,
        fileProcessingService: FileProcessingService,
    ): NfsStorageService = NfsStorageService(ftpService, filesService, folderResolver, fileProcessingService)

    @Bean
    fun nfsFileService(): NfsFilesService = NfsFilesService()

    @Bean
    fun nfsFtpService(): NfsFtpService = NfsFtpService(folderResolver)

    @Bean
    fun pageTabUtil(
        toSubmissionMapper: ToSubmissionMapper,
        toFileListMapper: ToFileListMapper,
    ): PageTabUtil = PageTabUtil(serializationService, toSubmissionMapper, toFileListMapper)

    @Bean
    fun fireFtpService(serializationService: ExtSerializationService): FireFtpService =
        FireFtpService(fireClient, serializationService)

    @Bean
    fun fireFilesService(): FireFilesService = FireFilesService(fireClient, File(properties.fireTempDirPath))

    @Bean
    fun pageTabService(
        pageTabUtil: PageTabUtil,
    ): PageTabService =
        PageTabService(
            File(properties.fireTempDirPath),
            pageTabUtil,
        )

//    @Bean
//    fun fireService(): FireService = FireService(fireClient, File(properties.fireTempDirPath))

    @Bean
    fun fileSystemService(
        nfsStorageService: NfsStorageService,
        fireStorageService: FireStorageService,
    ): FileSystemService = FileSystemService(nfsStorageService, fireStorageService)

    @Bean
    fun extFilesResolver() = FilesResolver(File(properties.requestFilesPath))
}
