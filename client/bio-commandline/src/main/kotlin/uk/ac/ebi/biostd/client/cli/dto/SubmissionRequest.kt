package uk.ac.ebi.biostd.client.cli.dto

import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import java.io.File

internal data class SubmissionRequest(
    val submissionFile: File,
    val timeout: Int,
    val securityConfig: SecurityConfig,
    val filesConfig: SubmissionFilesConfig,
)
