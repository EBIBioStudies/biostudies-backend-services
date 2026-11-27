package ac.uk.ebi.biostd.handlers.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import uk.ac.ebi.biostd.common.properties.NotificationProperties

@ConfigurationProperties(prefix = "app")
data class ApplicationProperties(
    @NestedConfigurationProperty
    val notifications: NotificationProperties,
)
