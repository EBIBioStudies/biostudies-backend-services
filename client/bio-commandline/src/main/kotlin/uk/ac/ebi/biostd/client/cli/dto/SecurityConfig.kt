package uk.ac.ebi.biostd.client.cli.dto

internal data class SecurityConfig(
    val server: String,
    val user: String,
    val password: String,
    val onBehalf: String? = null,
)
