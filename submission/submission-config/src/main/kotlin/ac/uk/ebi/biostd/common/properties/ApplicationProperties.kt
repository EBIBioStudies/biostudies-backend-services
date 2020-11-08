package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "app")
open class ApplicationProperties {
    lateinit var tempDirPath: String
    lateinit var submissionPath: String
    lateinit var ftpPath: String
    lateinit var instanceBaseUrl: String

    @NestedConfigurationProperty
    var security: SecurityProperties = SecurityProperties()

    @NestedConfigurationProperty
    var notifications: NotificationProperties = NotificationProperties()

    @NestedConfigurationProperty
    var fire: FireProperties = FireProperties()
}

class FireProperties {
    lateinit var host: String
    lateinit var username: String
    lateinit var password: String
}
