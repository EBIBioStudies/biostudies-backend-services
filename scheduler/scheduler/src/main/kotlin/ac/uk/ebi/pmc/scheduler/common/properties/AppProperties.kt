package ac.uk.ebi.pmc.scheduler.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "app")
@Component
class AppProperties {
    lateinit var appsFolder: String
}