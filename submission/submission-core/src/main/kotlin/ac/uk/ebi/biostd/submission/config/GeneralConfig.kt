package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.common.properties.FireProperties
import ac.uk.ebi.biostd.common.properties.FtpProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbServicesConfig
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ebi.ac.uk.coroutines.RetryConfig
import ebi.ac.uk.coroutines.SuspendRetryTemplate
import ebi.ac.uk.ftp.FtpClient
import ebi.ac.uk.ftp.RetryFtpClient
import ebi.ac.uk.paths.SubmissionFolderResolver
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
import uk.ac.ebi.fire.client.integration.web.S3Config
import uk.ac.ebi.io.builder.FilesSourceListBuilder
import uk.ac.ebi.io.config.FilesSourceConfig
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import kotlin.time.Duration.Companion.minutes

@Suppress("TooManyFunctions")
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
        submissionFolderResolver: SubmissionFolderResolver,
        fireClient: FireClient,
        @Qualifier(FILE_SOURCE_FTP_IN_CLIENT) fileSourceFtpInClient: FtpClient,
        @Qualifier(FTP_OUT_CLIENT) ftpOutClient: FtpClient,
        filesRepo: SubmissionFilesPersistenceService,
    ): FilesSourceConfig =
        FilesSourceConfig(
            submissionFolderResolver = submissionFolderResolver,
            fireClient = fireClient,
            filesRepository = filesRepo,
            ftpInClient = fileSourceFtpInClient,
            ftpOutClient = ftpOutClient,
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
            retryConfig(fireProps),
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
    @Qualifier(FTP_IN_RETRY_TEMPLATE)
    fun userFtpRetry(properties: ApplicationProperties): SuspendRetryTemplate {
        val retry = properties.security.filesProperties.ftpIn.retry
        val config =
            RetryConfig(
                maxAttempts = retry.maxAttempts,
                initialInterval = retry.initialInterval,
                multiplier = retry.multiplier,
                maxInterval = retry.maxInterval.minutes.inWholeMilliseconds,
            )
        return SuspendRetryTemplate(config)
    }

    @Bean
    @Qualifier(FTP_OUT_RETRY_TEMPLATE)
    fun subFtpRetry(properties: ApplicationProperties): SuspendRetryTemplate {
        val retry = properties.security.filesProperties.ftpOut.retry
        val config =
            RetryConfig(
                maxAttempts = retry.maxAttempts,
                initialInterval = retry.initialInterval,
                multiplier = retry.multiplier,
                maxInterval = retry.maxInterval.minutes.inWholeMilliseconds,
            )
        return SuspendRetryTemplate(config)
    }

    @Bean
    @Qualifier(FTP_IN_CLIENT)
    fun userFilesFtpClient(
        properties: ApplicationProperties,
        @Qualifier(FTP_IN_RETRY_TEMPLATE) template: SuspendRetryTemplate,
    ): FtpClient {
        val client = ftpClient(properties.security.filesProperties.ftpIn)
        return RetryFtpClient(template, client)
    }

    @Bean
    @Qualifier(FILE_SOURCE_FTP_IN_CLIENT)
    fun fileSourceFtpInClient(
        properties: ApplicationProperties,
        @Qualifier(FTP_IN_RETRY_TEMPLATE) template: SuspendRetryTemplate,
    ): FtpClient {
        val client = ftpClient(properties.security.filesProperties.ftpIn)
        return RetryFtpClient(template, client)
    }

    @Bean
    @Qualifier(FTP_OUT_CLIENT)
    fun ftpOutClient(
        properties: ApplicationProperties,
        @Qualifier(FTP_OUT_RETRY_TEMPLATE) template: SuspendRetryTemplate,
    ): FtpClient {
        val client = ftpClient(properties.security.filesProperties.ftpOut)
        return RetryFtpClient(template, client)
    }

    companion object {
        const val FTP_IN_CLIENT = "FtpInClient"
        const val FTP_IN_RETRY_TEMPLATE = "ftpInRetryTemplate"

        const val FTP_OUT_CLIENT = "ftpOutClient"
        const val FTP_OUT_RETRY_TEMPLATE = "ftoOutFtpRetryTemplate"

        const val FILE_SOURCE_FTP_IN_CLIENT = "fileSourceFtpInClient"

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

        fun retryConfig(properties: FireProperties) =
            RetryConfig(
                maxAttempts = properties.retry.maxAttempts,
                initialInterval = properties.retry.initialInterval,
                multiplier = properties.retry.multiplier,
                maxInterval = properties.retry.maxInterval.minutes.inWholeMilliseconds,
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
