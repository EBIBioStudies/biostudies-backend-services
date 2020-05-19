package ebi.ac.uk.notifications.integration

import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.util.Properties

class NotificationProperties {
    lateinit var smtp: String
    lateinit var uiUrl: String

    @NestedConfigurationProperty
    var rt: RtConfig = RtConfig()

    fun asProperties(): Properties = Properties().apply { setProperty("mail.host", smtp) }
}

class RtConfig {
    lateinit var host: String
    lateinit var queue: String
    lateinit var user: String
    lateinit var password: String
}
