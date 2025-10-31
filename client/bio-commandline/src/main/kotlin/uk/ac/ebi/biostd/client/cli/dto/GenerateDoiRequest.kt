package uk.ac.ebi.biostd.client.cli.dto

internal data class GenerateDoiRequest(
    val accNo: String,
    val securityConfig: SecurityConfig,
)
