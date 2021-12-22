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
    var ftp: Ftp = Ftp()

    @NestedConfigurationProperty
    var persistence: Persistence = Persistence()
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
