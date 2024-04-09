package uk.ac.ebi.scheduler.pmc.importer.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "pmc.import")
@Component
class PmcProcessorProp {
    lateinit var temp: String
    lateinit var mongoUri: String
    lateinit var mongoDatabase: String

    var loadFolder: String? = null
    var bioStudiesUrl: String? = null
    var bioStudiesUser: String? = null
    var bioStudiesPassword: String? = null
}
