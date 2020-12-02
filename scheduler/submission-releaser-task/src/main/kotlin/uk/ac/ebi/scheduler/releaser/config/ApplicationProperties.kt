package uk.ac.ebi.scheduler.releaser.config

import ac.uk.ebi.scheduler.properties.ReleaserMode
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "app")
@Component
class ApplicationProperties {
    lateinit var mode: ReleaserMode

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
    var firstWarningDays: Long = -1
    var secondWarningDays: Long = -1
    var thirdWarningDays: Long = -1
}
