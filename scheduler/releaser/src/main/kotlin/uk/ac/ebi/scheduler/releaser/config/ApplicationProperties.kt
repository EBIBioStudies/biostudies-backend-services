package uk.ac.ebi.scheduler.releaser.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "app")
@Component
class ApplicationProperties {
    lateinit var bioStudiesUrl: String
    lateinit var bioStudiesUser: String
    lateinit var bioStudiesPassword: String

    @NestedConfigurationProperty
    var notificationTimes = NotificationTimes()
}

class NotificationTimes {
    var firstWarning: Long = -1
    var secondWarning: Long = -1
    var thirdWarning: Long = -1
}
