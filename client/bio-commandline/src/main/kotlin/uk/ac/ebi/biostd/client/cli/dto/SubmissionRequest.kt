package uk.ac.ebi.biostd.client.cli.dto

import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.io.sources.PreferredSource
import java.io.File

internal data class SubmissionRequest(
    val securityConfig: SecurityConfig,
    val file: File,
    val attached: List<File>,
    val fileMode: FileMode,
    val preferredSource: PreferredSource,
)
