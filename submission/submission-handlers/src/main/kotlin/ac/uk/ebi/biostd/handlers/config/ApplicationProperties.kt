package ac.uk.ebi.biostd.handlers.config

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app")
class ApplicationProperties {
    @NestedConfigurationProperty
    var notifications: NotificationProperties = NotificationProperties()
}
