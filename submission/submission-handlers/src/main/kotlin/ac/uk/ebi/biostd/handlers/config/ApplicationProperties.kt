package ac.uk.ebi.biostd.handlers.config

import ac.uk.ebi.biostd.common.properties.NotificationProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "app")
@ConstructorBinding
data class ApplicationProperties(
    val notifications: NotificationProperties,
)
