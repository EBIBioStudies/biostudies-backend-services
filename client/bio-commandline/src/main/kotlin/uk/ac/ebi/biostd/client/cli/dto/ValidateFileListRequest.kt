package uk.ac.ebi.biostd.client.cli.dto

internal data class ValidateFileListRequest(
    val fileListPath: String,
    val accNo: String? = null,
    val rootPath: String? = null,
    val securityConfig: SecurityConfig,
)
