package ac.uk.ebi.biostd.pmc

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.pmc.PmcLinksProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PmcPipelineConfig {
    @Bean
    fun pmcScheduller(
        applicationProperties: ApplicationProperties,
        pmcLinksProcessor: PmcLinksProcessor,
    ): PmcScheduler = PmcScheduler(pmcLinksProcessor, applicationProperties.pmc)
}
