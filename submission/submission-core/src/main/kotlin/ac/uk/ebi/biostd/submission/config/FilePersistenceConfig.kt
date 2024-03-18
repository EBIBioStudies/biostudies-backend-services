package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.doc.integration.ToSubmissionConfig
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.api.NfsReleaseMode
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabUtil
import ac.uk.ebi.biostd.persistence.filesystem.service.StorageService
import ac.uk.ebi.biostd.persistence.integration.config.SqlPersistenceConfig
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File

@Configuration
@Import(value = [SqlPersistenceConfig::class, GeneralConfig::class, ToSubmissionConfig::class])
class FilePersistenceConfig(
    private val folderResolver: SubmissionFolderResolver,
    private val properties: ApplicationProperties,
    private val serializationService: SerializationService,
    private val fireClient: FireClient,
) {
    @Bean
    @Suppress("LongParameterList")
    fun fileStorageService(
        fireFtpService: FireFtpService,
        fireFilesService: FireFilesService,
        nfsFtpService: NfsFtpService,
        nfsFilesService: NfsFilesService,
        extSerializationService: ExtSerializationService,
    ): FileStorageService =
        StorageService(fireFtpService, fireFilesService, nfsFtpService, nfsFilesService, extSerializationService)

    @Bean
    fun nfsFtpService(): NfsFtpService =
        NfsFtpService(NfsReleaseMode.valueOf(properties.persistence.nfsReleaseMode), folderResolver)

    @Bean
    fun nfsFileService(): NfsFilesService = NfsFilesService(fireClient, folderResolver)

    @Bean
    fun fireFtpService(): FireFtpService = FireFtpService(fireClient)

    @Bean
    fun fireFileService(): FireFilesService = FireFilesService(fireClient)

    @Bean
    fun pageTabService(
        pageTabUtil: PageTabUtil,
    ): PageTabService = PageTabService(File(properties.fire.tempDirPath), pageTabUtil)

    @Bean
    fun pageTabUtil(
        toSubmissionMapper: ToSubmissionMapper,
        toFileListMapper: ToFileListMapper,
    ): PageTabUtil = PageTabUtil(serializationService, toSubmissionMapper, toFileListMapper)
}
