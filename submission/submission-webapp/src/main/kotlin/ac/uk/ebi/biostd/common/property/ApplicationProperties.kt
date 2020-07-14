package ac.uk.ebi.biostd.common.property

import ebi.ac.uk.notifications.integration.NotificationProperties
import ebi.ac.uk.security.integration.SecurityProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "app")
open class ApplicationProperties {
    lateinit var tempDirPath: String
    lateinit var submissionPath: String
    lateinit var ftpPath: String
    lateinit var magicPath: String
    lateinit var instanceBaseUrl: String

    @NestedConfigurationProperty
    var security: SecurityProperties = SecurityProperties()

    @NestedConfigurationProperty
    var notifications: NotificationProperties = NotificationProperties()
}
