package ac.uk.ebi.pmc.scheduler.pmc.importer

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "pmc.import")
@Component
class PmcProcessorProp {

    lateinit var temp: String
    lateinit var mongoUri: String
    var bioStudiesUrl: String? = null
    var bioStudiesUser: String? = null
    var bioStudiesPassword: String? = null
}
