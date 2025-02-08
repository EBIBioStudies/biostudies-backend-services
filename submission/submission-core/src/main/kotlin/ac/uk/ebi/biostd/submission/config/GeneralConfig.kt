package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.common.properties.FtpProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbServicesConfig
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ebi.ac.uk.ftp.FtpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.api.LocalClusterClient
import uk.ac.ebi.biostd.client.cluster.api.LsfClusterClient
import uk.ac.ebi.biostd.client.cluster.api.SlurmClusterClient
import uk.ac.ebi.biostd.client.cluster.model.Cluster.LSF
import uk.ac.ebi.biostd.client.cluster.model.Cluster.SLURM
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.integration.web.FireClientFactory
import uk.ac.ebi.fire.client.integration.web.FireConfig
import uk.ac.ebi.fire.client.integration.web.RetryConfig
import uk.ac.ebi.fire.client.integration.web.S3Config
import uk.ac.ebi.fire.client.retry.SuspendRetryTemplate
import uk.ac.ebi.io.builder.FilesSourceListBuilder
import uk.ac.ebi.io.config.FilesSourceConfig
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import java.nio.file.Paths
import kotlin.time.Duration.Companion.minutes

@Configuration
@Import(MongoDbServicesConfig::class)
@EnableConfigurationProperties(ApplicationProperties::class)
class GeneralConfig {
    @Bean
    fun filesSourceListBuilder(config: FilesSourceConfig): FilesSourceListBuilder = config.filesSourceListBuilder()

    @Bean
    fun fileSourcesService(builder: FilesSourceListBuilder): FileSourcesService = FileSourcesService(builder)

    @Bean
    fun extFilesResolver(properties: ApplicationProperties): FilesResolver = FilesResolver(File(properties.persistence.requestFilesPath))

    @Bean
    fun filesSourceConfig(
        fireClient: FireClient,
        @Qualifier(FILE_SOURCE_FTP_CLIENT) ftpClient: FtpClient,
        applicationProperties: ApplicationProperties,
        filesRepo: SubmissionFilesPersistenceService,
    ): FilesSourceConfig =
        FilesSourceConfig(
            submissionPath = Paths.get(applicationProperties.persistence.privateSubmissionsPath),
            fireClient = fireClient,
            filesRepository = filesRepo,
            ftpClient = ftpClient,
            retryTemplate = SuspendRetryTemplate(retryConfig(applicationProperties)),
            checkFilesPath = applicationProperties.checkFilesPath,
        )

    @Bean
    fun fireClient(properties: ApplicationProperties): FireClient {
        val fireProps = properties.fire
        return FireClientFactory.create(
            FireConfig(
                fireHost = fireProps.host,
                fireVersion = fireProps.version,
                username = fireProps.username,
                password = properties.fire.password,
            ),
            S3Config(
                accessKey = fireProps.s3.accessKey,
                secretKey = fireProps.s3.secretKey,
                region = fireProps.s3.region,
                endpoint = fireProps.s3.endpoint,
                bucket = fireProps.s3.bucket,
            ),
            retryConfig(properties),
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.cluster", name = ["enabled"], havingValue = "true")
    fun clusterClient(properties: ApplicationProperties): ClusterClient =
        when (properties.cluster.default) {
            LSF -> lsfCluster(properties)
            SLURM -> slurmCluster(properties)
        }

    @Bean
    @ConditionalOnProperty(prefix = "app.cluster", name = ["enabled"], havingValue = "false")
    fun localClusterClient(): ClusterClient = LocalClusterClient()

    @Bean
    @Qualifier(USER_FILES_FTP_CLIENT)
    fun userFilesFtpClient(properties: ApplicationProperties) = ftpClient(properties.security.filesProperties.ftp)

    @Bean
    @Qualifier(FILE_SOURCE_FTP_CLIENT)
    fun fileSourceFtpClient(properties: ApplicationProperties) = ftpClient(properties.security.filesProperties.ftp)

    companion object {
        const val FILE_SOURCE_FTP_CLIENT = "fileSourceFtpClient"
        const val USER_FILES_FTP_CLIENT = "userFilesFtpClient"

        fun ftpClient(ftpProperties: FtpProperties): FtpClient =
            FtpClient.create(
                ftpUser = ftpProperties.ftpUser,
                ftpPassword = ftpProperties.ftpPassword,
                ftpUrl = ftpProperties.ftpUrl,
                ftpPort = ftpProperties.ftpPort,
                ftpRootPath = ftpProperties.ftpRootPath,
                defaultTimeout = ftpProperties.defaultTimeout,
                connectionTimeout = ftpProperties.connectionTimeout,
            )

        fun lsfCluster(properties: ApplicationProperties): LsfClusterClient =
            LsfClusterClient.create(
                properties.cluster.key,
                properties.cluster.lsfServer,
                properties.cluster.logsPath,
            )

        fun retryConfig(properties: ApplicationProperties) =
            RetryConfig(
                maxAttempts = properties.fire.retry.maxAttempts,
                initialInterval = properties.fire.retry.initialInterval,
                multiplier = properties.fire.retry.multiplier,
                maxInterval = properties.fire.retry.maxInterval.minutes.inWholeMilliseconds,
            )

        fun slurmCluster(properties: ApplicationProperties): SlurmClusterClient =
            SlurmClusterClient.create(
                properties.cluster.key,
                properties.cluster.slurmServer,
                properties.cluster.logsPath,
                properties.cluster.wrapperPath,
            )
    }
}
