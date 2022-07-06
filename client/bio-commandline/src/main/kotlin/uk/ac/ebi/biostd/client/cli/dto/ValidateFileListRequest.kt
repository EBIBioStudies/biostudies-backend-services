package uk.ac.ebi.biostd.client.cli.dto

internal data class ValidateFileListRequest(
    val securityConfig: SecurityConfig,
    val fileListPath: String
)
