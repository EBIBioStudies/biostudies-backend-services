package ebi.ac.uk.api.security

class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val superUser: Boolean = false,
    var instanceKey: String? = null,
    var path: String? = null,
    val notificationsEnabled: Boolean = false
)
