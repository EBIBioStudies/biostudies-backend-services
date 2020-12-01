package ac.uk.ebi.biostd.persistence.doc.integration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class AppProperties {
    lateinit var connection: String
}
