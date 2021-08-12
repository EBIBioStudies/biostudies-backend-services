package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.NestedConfigurationProperty

class SecurityProperties {
    lateinit var tokenHash: String
    lateinit var environment: String
    lateinit var filesDirPath: String
    lateinit var captchaKey: String
    lateinit var magicDirPath: String

    var checkCaptcha: Boolean = false
    var requireActivation: Boolean = false

    @NestedConfigurationProperty
    var instanceKeys = InstanceKeys()
}

class InstanceKeys {
    lateinit var dev: String
    lateinit var beta: String
    lateinit var prod: String
}
