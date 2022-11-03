package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.domain.helpers.TempFileGenerator
import ac.uk.ebi.biostd.submission.helpers.FireFilesSourceFactory
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.fire.client.integration.web.FireClient

@Configuration
@EnableConfigurationProperties(ApplicationProperties::class)
internal class GeneralConfig {
    @Bean
    fun tempFileGenerator(properties: ApplicationProperties) = TempFileGenerator(properties)

    @Bean
    fun fireFilesSourceFactory(
        fireClient: FireClient,
        queryService: SubmissionPersistenceQueryService,
    ) = FireFilesSourceFactory(fireClient, queryService)

    @Bean
    fun fileSourcesService(
        applicationProperties: ApplicationProperties,
        filesSourceFactory: FireFilesSourceFactory,
    ) = FileSourcesService(applicationProperties, filesSourceFactory)
}
