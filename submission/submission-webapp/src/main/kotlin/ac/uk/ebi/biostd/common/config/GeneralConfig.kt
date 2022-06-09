package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.domain.helpers.FireFilesSourceFactory
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.helpers.TempFileGenerator
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
        applicationProperties: ApplicationProperties,
        fireClient: FireClient
    ) = FireFilesSourceFactory(applicationProperties.persistence, fireClient)

    @Bean
    fun sourceGenerator(
        applicationProperties: ApplicationProperties,
        filesSourceFactory: FireFilesSourceFactory
    ) = SourceGenerator(applicationProperties, filesSourceFactory)
}
