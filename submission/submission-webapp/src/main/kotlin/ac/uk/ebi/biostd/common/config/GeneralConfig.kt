package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.stats.web.TempFileGenerator
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ebi.ac.uk.ftp.FtpClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.io.builder.FilesSourceListBuilder
import uk.ac.ebi.io.config.FilesSourceConfig
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import java.nio.file.Paths

@Configuration
@EnableConfigurationProperties(ApplicationProperties::class)
internal class GeneralConfig {
    @Bean
    fun tempFileGenerator(properties: ApplicationProperties): TempFileGenerator =
        TempFileGenerator(properties.tempDirPath)

    @Bean
    fun filesSourceConfig(
        fireClient: FireClient,
        ftpClient: FtpClient,
        applicationProperties: ApplicationProperties,
        filesRepo: SubmissionFilesPersistenceService,
    ): FilesSourceConfig =
        FilesSourceConfig(Paths.get(applicationProperties.submissionPath), fireClient, filesRepo, ftpClient)

    @Bean
    fun filesSourceListBuilder(config: FilesSourceConfig): FilesSourceListBuilder = config.filesSourceListBuilder()

    @Bean
    fun fileSourcesService(builder: FilesSourceListBuilder): FileSourcesService = FileSourcesService(builder)

    @Bean
    fun extFilesResolver(properties: ApplicationProperties): FilesResolver =
        FilesResolver(File(properties.requestFilesPath))
}
