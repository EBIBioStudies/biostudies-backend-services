package uk.ac.ebi.biostd.api.security

data class ActivateByEmailRequest(
    val email: String,
    val instanceKey: String,
    val path: String,
)
