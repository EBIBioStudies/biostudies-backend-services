package ebi.ac.uk.api.security

data class GetOrRegisterUserRequest(
    val register: Boolean,
    val userEmail: String,
    val userName: String?
)
