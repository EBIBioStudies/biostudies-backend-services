package uk.ac.ebi.biostd.client.cli.dto

import uk.ac.ebi.biostd.api.SubmitParameters
import java.io.File

internal data class SubmissionRequest(
    val submissionFile: File,
    val await: Boolean,
    val securityConfig: SecurityConfig,
    val parameters: SubmitParameters,
    val files: List<File>,
)
