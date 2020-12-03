package uk.ac.ebi.scheduler.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "app")
@Component
class AppProperties {
    lateinit var appsFolder: String
    lateinit var notificationsUrl: String

    @NestedConfigurationProperty
    var dailyScheduling: DailyScheduling = DailyScheduling()
}

class DailyScheduling {
    var pmc: Boolean = false
    var notifier: Boolean = false
    var releaser: Boolean = true
}
