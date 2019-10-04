package ebi.ac.uk.api.dto

import java.time.OffsetDateTime

data class SubmissionDto(
    val accno: String,
    val title: String,
    val version: Int,
    val ctime: OffsetDateTime,
    val mtime: OffsetDateTime,
    val rtime: OffsetDateTime
)
