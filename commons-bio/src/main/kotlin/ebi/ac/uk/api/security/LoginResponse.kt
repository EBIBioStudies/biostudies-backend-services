package ebi.ac.uk.api.security

class LoginResponse(
        val sessid: String,
        val email: String,
        val status: String = "OK",
        val username: String,
        val secret: String
)

