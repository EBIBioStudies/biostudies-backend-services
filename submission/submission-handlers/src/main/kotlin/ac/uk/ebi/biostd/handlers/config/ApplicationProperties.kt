package ac.uk.ebi.biostd.handlers.config

import ebi.ac.uk.notifications.integration.NotificationProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app")
class ApplicationProperties {
    @NestedConfigurationProperty
    var notifications: NotificationProperties = NotificationProperties()
}
