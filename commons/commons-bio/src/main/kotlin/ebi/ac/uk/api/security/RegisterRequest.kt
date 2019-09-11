package ebi.ac.uk.api.security

class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val superUser: Boolean = false,
    var instanceKey: String? = null,
    var path: String? = null
)
