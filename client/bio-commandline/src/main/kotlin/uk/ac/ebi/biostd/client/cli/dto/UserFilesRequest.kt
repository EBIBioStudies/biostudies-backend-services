package uk.ac.ebi.biostd.client.cli.dto

import java.io.File

internal data class UserFilesRequest(
    val files: List<File>,
    val relPath: String,
    val securityConfig: SecurityConfig,
)
