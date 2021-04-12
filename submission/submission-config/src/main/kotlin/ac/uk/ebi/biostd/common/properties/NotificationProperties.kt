package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.lang.System.setProperty
import java.util.Properties

class NotificationProperties {
    lateinit var smtp: String
    lateinit var uiUrl: String
    lateinit var slackUrl: String

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
