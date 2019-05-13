package ebi.ac.uk.security.integration

class SecurityProperties {

    lateinit var tokenHash: String
    lateinit var environment: String
    var requireActivation: Boolean = false
}
