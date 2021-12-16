package uk.ac.ebi.scheduler.pmc.exporter.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "pmc.export")
@Component
class PmcExporterProperties {
    lateinit var fileName: String
    lateinit var outputPath: String

    @NestedConfigurationProperty
    var bioStudies: BioStudies = BioStudies()

    @NestedConfigurationProperty
    var ftp: Ftp = Ftp()
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
