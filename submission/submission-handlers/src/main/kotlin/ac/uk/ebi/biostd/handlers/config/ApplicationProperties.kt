package ac.uk.ebi.biostd.handlers.config

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "app")
data class ApplicationProperties(
    val baseInstanceUrl: String,
    @NestedConfigurationProperty
    val notifications: NotificationProperties,
)
