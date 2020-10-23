package uk.ac.ebi.scheduler.releaser.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "app")
@Component
class ApplicationProperties {
    @NestedConfigurationProperty
    var bioStudies: BioStudies = BioStudies()

    @NestedConfigurationProperty
    var notificationTimes: NotificationTimes = NotificationTimes()
}

class BioStudies {
    lateinit var url: String
    lateinit var user: String
    lateinit var password: String
}

class NotificationTimes {
    var firstWarning: Long = -1
    var secondWarning: Long = -1
    var thirdWarning: Long = -1
}
