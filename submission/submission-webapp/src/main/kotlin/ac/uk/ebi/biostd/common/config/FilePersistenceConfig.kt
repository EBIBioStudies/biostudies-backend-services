package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
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
import uk.ac.ebi.fire.client.integration.web.FireClientFactory
import uk.ac.ebi.fire.client.integration.web.FireConfig
import uk.ac.ebi.fire.client.integration.web.RetryConfig
import uk.ac.ebi.fire.client.integration.web.S3Config
import java.io.File
import kotlin.time.Duration.Companion.minutes

@Configuration
@Import(value = [SqlPersistenceConfig::class, GeneralConfig::class])
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
    fun nfsFtpService(): NfsFtpService = NfsFtpService(folderResolver)

    @Bean
    fun nfsFileService(): NfsFilesService = NfsFilesService(fireClient, folderResolver)

    @Bean
    fun fireFtpService(): FireFtpService = FireFtpService(fireClient)

    @Bean
    fun fireFileService(): FireFilesService = FireFilesService(fireClient)

    @Bean
    fun pageTabService(
        pageTabUtil: PageTabUtil,
    ): PageTabService = PageTabService(File(properties.fireTempDirPath), pageTabUtil)

    @Bean
    fun pageTabUtil(
        toSubmissionMapper: ToSubmissionMapper,
        toFileListMapper: ToFileListMapper,
    ): PageTabUtil = PageTabUtil(serializationService, toSubmissionMapper, toFileListMapper)

    @Bean
    fun fireClient(properties: ApplicationProperties): FireClient {
        val fireProps = properties.fire
        val retryProps = fireProps.retry
        return FireClientFactory.create(
            FireConfig(
                fireHost = fireProps.host,
                fireVersion = fireProps.version,
                username = fireProps.username,
                password = properties.fire.password
            ),
            S3Config(
                accessKey = fireProps.s3.accessKey,
                secretKey = fireProps.s3.secretKey,
                region = fireProps.s3.region,
                endpoint = fireProps.s3.endpoint,
                bucket = fireProps.s3.bucket
            ),
            RetryConfig(
                maxAttempts = retryProps.maxAttempts,
                initialInterval = retryProps.initialInterval,
                multiplier = retryProps.multiplier,
                maxInterval = retryProps.maxInterval.minutes.inWholeMilliseconds,
            )
        )
    }
}
