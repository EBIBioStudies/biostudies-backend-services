package ac.uk.ebi.biostd.common.property

import ebi.ac.uk.notifications.integration.NotificationProperties
import ebi.ac.uk.paths.SUBMISSION_PATH
import ebi.ac.uk.security.integration.SecurityProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.nio.file.Path
import java.nio.file.Paths

@ConfigurationProperties(prefix = "app")
open class ApplicationProperties {
    lateinit var basepath: String
    lateinit var tempDirPath: String
    lateinit var instanceBaseUrl: String

    val submissionsPath: Path
        get() = Paths.get(basepath).resolve(SUBMISSION_PATH)

    @NestedConfigurationProperty
    var security: SecurityProperties = SecurityProperties()

    @NestedConfigurationProperty
    var notifications: NotificationProperties = NotificationProperties()
}
