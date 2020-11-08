package ac.uk.ebi.biostd.common.properties

class SecurityProperties {

    lateinit var tokenHash: String
    lateinit var environment: String
    lateinit var filesDirPath: String
    lateinit var captchaKey: String
    lateinit var magicDirPath: String

    var checkCaptcha: Boolean = false
    var requireActivation: Boolean = false
}
