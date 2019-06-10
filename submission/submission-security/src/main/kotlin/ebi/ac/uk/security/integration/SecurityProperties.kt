package ebi.ac.uk.security.integration

class SecurityProperties {

    lateinit var tokenHash: String
    lateinit var environment: String
    lateinit var basePath: String
    lateinit var filesDirPath: String
    var requireActivation: Boolean = false
}
