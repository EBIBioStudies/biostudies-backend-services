package uk.ac.ebi.biostd.client.cli.dto

internal data class DeleteUserFilesRequest(
    val fileName: String,
    val relPath: String,
    val securityConfig: SecurityConfig,
)
