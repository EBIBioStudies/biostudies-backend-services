package ebi.ac.uk.api.dto

import java.time.OffsetDateTime

data class SubmissionDto(
    val accno: String,
    val title: String,
    val mtime: OffsetDateTime,
    val rtime: OffsetDateTime?,
    val status: String,
    val errors: List<String>,
)
