package uk.ac.ebi.scheduler.releaser.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "releaser")
@Component
class SubmissionReleaserProperties {
    @NestedConfigurationProperty
    var bioStudies: BioStudies = BioStudies()

    @NestedConfigurationProperty
    var rabbitmq: Rabbitmq = Rabbitmq()

    @NestedConfigurationProperty
    var notificationTimes: NotificationTimes = NotificationTimes()
}

class BioStudies {
    lateinit var url: String
    lateinit var user: String
    lateinit var password: String
}

class Rabbitmq {
    lateinit var host: String
    lateinit var user: String
    lateinit var password: String
    var port: Long = -1
}

class NotificationTimes {
    var firstWarning: Long = -1
    var secondWarning: Long = -1
    var thirdWarning: Long = -1
}
