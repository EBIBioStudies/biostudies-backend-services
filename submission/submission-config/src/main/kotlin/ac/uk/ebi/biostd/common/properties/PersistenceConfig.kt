package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.persistence")
@Suppress("UnusedPrivateMember")
class PersistenceConfig {
    private var enableMongo: Boolean = false
}
