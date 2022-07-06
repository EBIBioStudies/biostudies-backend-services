package uk.ac.ebi.biostd.client.cli.dto

internal data class DeletionRequest(
    val securityConfig: SecurityConfig,
    val accNoList: List<String>,
)
