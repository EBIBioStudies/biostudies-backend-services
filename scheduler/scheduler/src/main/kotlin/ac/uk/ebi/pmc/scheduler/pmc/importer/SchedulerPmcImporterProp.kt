package ac.uk.ebi.pmc.scheduler.pmc.importer

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "pmc.import")
@Component
class SchedulerPmcImporterProp {

    lateinit var temp: String
    lateinit var mongoUri: String
    lateinit var bioStudiesUrl: String
    lateinit var bioStudiesUser: String
    lateinit var bioStudiesPassword: String
}
