package ebi.ac.uk.api.security

import javax.validation.constraints.Email

class RegisterRequest(
    val name: String,

    @field:Email(message = "The provided email is not valid")
    val email: String,
    val password: String,
    val superUser: Boolean = false,
    var instanceKey: String? = null,
    var path: String? = null,
    val notificationsEnabled: Boolean = false
)
