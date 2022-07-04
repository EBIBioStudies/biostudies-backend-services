package uk.ac.ebi.scheduler.exporter.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "exporter")
@Component
class ExporterProperties {
    lateinit var tmpFilesPath: String

    @NestedConfigurationProperty
    var bioStudies: BioStudies = BioStudies()

    @NestedConfigurationProperty
    var ftp: Ftp = Ftp()

    @NestedConfigurationProperty
    var persistence: Persistence = Persistence()

    @NestedConfigurationProperty
    var pmc: Pmc = Pmc()

    @NestedConfigurationProperty
    var publicOnly: PublicOnly = PublicOnly()
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

class Persistence {
    lateinit var uri: String
    lateinit var database: String
}

class Pmc {
    lateinit var fileName: String
    lateinit var outputPath: String
}

class PublicOnly {
    lateinit var fileName: String
    lateinit var outputPath: String
}
