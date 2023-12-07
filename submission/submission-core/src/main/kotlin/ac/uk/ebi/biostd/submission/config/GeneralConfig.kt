package ac.uk.ebi.biostd.submission.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.common.properties.FilesProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbServicesConfig
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ebi.ac.uk.ftp.FtpClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.api.LocalClusterClient
import uk.ac.ebi.biostd.client.cluster.api.RemoteClusterClient
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.integration.web.FireClientFactory
import uk.ac.ebi.fire.client.integration.web.FireConfig
import uk.ac.ebi.fire.client.integration.web.RetryConfig
import uk.ac.ebi.fire.client.integration.web.S3Config
import uk.ac.ebi.io.builder.FilesSourceListBuilder
import uk.ac.ebi.io.config.FilesSourceConfig
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import java.nio.file.Paths
import kotlin.time.Duration.Companion.minutes

@Configuration
@Import(MongoDbServicesConfig::class)
@EnableConfigurationProperties(ApplicationProperties::class)
internal class GeneralConfig {
    @Bean
    fun filesSourceListBuilder(config: FilesSourceConfig): FilesSourceListBuilder = config.filesSourceListBuilder()

    @Bean
    fun fileSourcesService(builder: FilesSourceListBuilder): FileSourcesService = FileSourcesService(builder)

    @Bean
    fun extFilesResolver(properties: ApplicationProperties): FilesResolver =
        FilesResolver(File(properties.requestFilesPath))

    @Bean
    fun filesSourceConfig(
        fireClient: FireClient,
        ftpClient: FtpClient,
        applicationProperties: ApplicationProperties,
        filesRepo: SubmissionFilesPersistenceService,
    ): FilesSourceConfig =
        FilesSourceConfig(Paths.get(applicationProperties.submissionPath), fireClient, filesRepo, ftpClient)

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

    @Bean
    @ConditionalOnProperty(prefix = "app.cluster", name = ["enabled"], havingValue = "true")
    fun clusterClient(
        properties: ApplicationProperties,
    ): ClusterClient = RemoteClusterClient.create(
        properties.cluster.key,
        properties.cluster.server,
        properties.cluster.logsPath,
    )

    @Bean
    @ConditionalOnProperty(prefix = "app.cluster", name = ["enabled"], havingValue = "false")
    fun localClusterClient(): ClusterClient = LocalClusterClient()

    @Bean
    fun ftpClient(properties: ApplicationProperties) = ftpClient(properties.security.filesProperties)

    companion object {
        fun ftpClient(fileProperties: FilesProperties): FtpClient {
            return FtpClient.create(
                ftpUser = fileProperties.ftpUser,
                ftpPassword = fileProperties.ftpPassword,
                ftpUrl = fileProperties.ftpUrl,
                ftpPort = fileProperties.ftpPort,
            )
        }
    }
}
