package ac.uk.ebi.biostd.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
open class ApplicationProperties {

    lateinit var basepath: String
    lateinit var environment: String
    lateinit var tokenHash: String
}
