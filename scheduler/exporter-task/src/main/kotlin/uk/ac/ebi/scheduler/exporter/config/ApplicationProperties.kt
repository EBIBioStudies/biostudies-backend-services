package uk.ac.ebi.scheduler.exporter.config

import ac.uk.ebi.scheduler.properties.ExporterMode
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "app")
@Component
class ApplicationProperties {
    lateinit var fileName: String
    lateinit var outputPath: String
    lateinit var mode: ExporterMode

    @NestedConfigurationProperty
    var bioStudies: BioStudies = BioStudies()

    @NestedConfigurationProperty
    var ftp: Ftp = Ftp()
}

class BioStudies {
    lateinit var url: String
    lateinit var user: String
    lateinit var password: String
}

class Ftp {
    lateinit var host: String
    lateinit var user: String
    lateinit var password: String
    lateinit var port: Number
}
