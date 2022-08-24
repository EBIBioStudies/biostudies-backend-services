package uk.ac.ebi.biostd.client.cli.dto

import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ebi.ac.uk.extended.model.StorageMode
import java.io.File

internal data class SubmissionRequest(
    val submissionFile: File,
    val storageMode: StorageMode?,
    val securityConfig: SecurityConfig,
    val filesConfig: SubmissionFilesConfig,
)
