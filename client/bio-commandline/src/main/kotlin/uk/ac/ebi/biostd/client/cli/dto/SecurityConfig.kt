package uk.ac.ebi.biostd.client.cli.dto

// TODO: use here OnBehalfWebRequest
internal data class SecurityConfig(
    val server: String,
    val user: String,
    val password: String,
    val onBehalf: String? = null,
)
