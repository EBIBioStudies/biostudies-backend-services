package uk.ac.ebi.biostd.client.cli.dto

import java.io.File

internal data class UploadUserFilesRequest(
    val file: File,
    val relPath: String,
    val securityConfig: SecurityConfig,
)
