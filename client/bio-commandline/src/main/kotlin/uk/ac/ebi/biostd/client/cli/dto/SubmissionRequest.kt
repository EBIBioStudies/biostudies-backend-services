package uk.ac.ebi.biostd.client.cli.dto

import java.io.File

data class SubmissionRequest(
    val server: String,
    val user: String,
    val password: String,
    val onBehalf: String?,
    val file: File,
    val attached: List<File>
)
