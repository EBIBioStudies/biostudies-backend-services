package ac.uk.ebi.biostd.pmc

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.pmc.PmcLinksProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@Profile("!openapi-gen")
class PmcPipelineConfig {
    @Bean
    fun pmcScheduler(
        applicationProperties: ApplicationProperties,
        pmcLinksProcessor: PmcLinksProcessor,
    ): PmcScheduler = PmcScheduler(pmcLinksProcessor, applicationProperties.pmc)
}
