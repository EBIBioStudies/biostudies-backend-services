package ac.uk.ebi.pmc.config

import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties
class PropConfig {
    @Bean
    @ConfigurationProperties("app.data")
    fun properties() =
        PmcImporterProperties()
}