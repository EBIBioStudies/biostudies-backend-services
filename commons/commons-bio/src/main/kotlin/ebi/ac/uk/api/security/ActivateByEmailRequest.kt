package ebi.ac.uk.api.security

data class ActivateByEmailRequest(
    val email: String,
    val instanceKey: String,
    val path: String
)
