package ac.uk.ebi.biostd.common.property

import ebi.ac.uk.security.integration.SecurityProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "app")
open class ApplicationProperties {

    lateinit var basepath: String
    lateinit var environment: String

    @NestedConfigurationProperty
    var security: SecurityProperties = SecurityProperties()
}
