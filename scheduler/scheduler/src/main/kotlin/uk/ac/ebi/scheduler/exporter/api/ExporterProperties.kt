package uk.ac.ebi.scheduler.exporter.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component
import uk.ac.ebi.scheduler.releaser.api.BioStudies

@ConfigurationProperties(prefix = "exporter")
@Component
class ExporterProperties {
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
