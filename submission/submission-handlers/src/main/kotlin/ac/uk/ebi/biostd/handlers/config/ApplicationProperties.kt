package ac.uk.ebi.biostd.handlers.config

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "app")
@ConstructorBinding
data class ApplicationProperties(
    @NestedConfigurationProperty
    val notifications: NotificationProperties,
)
