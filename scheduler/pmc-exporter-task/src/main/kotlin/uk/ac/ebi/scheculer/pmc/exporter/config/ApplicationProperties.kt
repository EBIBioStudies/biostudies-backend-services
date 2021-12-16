package uk.ac.ebi.scheculer.pmc.exporter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "app")
@Component
class ApplicationProperties {
    lateinit var fileName: String
    lateinit var outputPath: String

    @NestedConfigurationProperty
    var ftp: Ftp = Ftp()

    @NestedConfigurationProperty
    var bioStudies: BioStudies = BioStudies()
}

class Ftp {
    lateinit var host: String
    lateinit var user: String
    lateinit var password: String
    lateinit var port: Number
}

class BioStudies {
    lateinit var url: String
    lateinit var user: String
    lateinit var password: String
}
