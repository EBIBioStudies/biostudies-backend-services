package uk.ac.ebi.scheduler.exporter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "app")
@Component
class ApplicationProperties {
    lateinit var fileName: String
    lateinit var outputPath: String

    @NestedConfigurationProperty
    var bioStudies: BioStudies = BioStudies()
}

class BioStudies {
    lateinit var url: String
    lateinit var user: String
    lateinit var password: String
}
