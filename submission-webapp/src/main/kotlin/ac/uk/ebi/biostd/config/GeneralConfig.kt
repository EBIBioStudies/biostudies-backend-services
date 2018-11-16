package ac.uk.ebi.biostd.config

import ac.uk.ebi.biostd.property.ApplicationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ApplicationProperties::class)
class GeneralConfig
