package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "app")
open class ApplicationProperties {
    lateinit var tempDirPath: String
    lateinit var submissionPath: String
    lateinit var ftpPath: String
    lateinit var instanceBaseUrl: String

    val fireTempDirPath get() = "$tempDirPath/fire-temp"
    val webTempDirPath get() = "$tempDirPath/web-temp"

    @NestedConfigurationProperty
    var security: SecurityProperties = SecurityProperties()

    @NestedConfigurationProperty
    var notifications: NotificationProperties = NotificationProperties()

    @NestedConfigurationProperty
    var fire: FireProperties = FireProperties()

    @NestedConfigurationProperty
    var mongo: MongoModuleProperties = MongoModuleProperties()

    @NestedConfigurationProperty
    var validator: ValidatorProperties = ValidatorProperties()

    @NestedConfigurationProperty
    var persistence: PersistenceProperties = PersistenceProperties()
}

class FireProperties {
    lateinit var host: String
    lateinit var username: String
    lateinit var password: String
}

class MongoModuleProperties {
    lateinit var connection: String
}

class ValidatorProperties {
    lateinit var euToxRiskValidationApi: String
}

class PersistenceProperties {
    var enableMongo: Boolean = false
    var enableFire: Boolean = false
}
