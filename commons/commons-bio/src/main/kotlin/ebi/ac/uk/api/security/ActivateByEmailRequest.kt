package ebi.ac.uk.api.security

class ActivateByEmailRequest(
    val email: String,
    val instanceKey: String,
    val path: String
)
