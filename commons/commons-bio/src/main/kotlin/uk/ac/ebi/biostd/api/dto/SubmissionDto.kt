package uk.ac.ebi.biostd.api.dto

import java.time.OffsetDateTime

data class SubmissionDto(
    val accno: String,
    val displayAccNo: String,
    val newSubmission: Boolean,
    val title: String,
    val mtime: OffsetDateTime,
    val rtime: OffsetDateTime?,
    val status: String,
    val errors: List<String>,
)
