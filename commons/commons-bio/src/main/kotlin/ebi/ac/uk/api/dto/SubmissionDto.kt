package ebi.ac.uk.api.dto

import ebi.ac.uk.model.SubmissionMethod
import java.time.OffsetDateTime

data class SubmissionDto(
    val accno: String,
    val title: String,
    val version: Int,
    val ctime: OffsetDateTime,
    val mtime: OffsetDateTime,
    val rtime: OffsetDateTime?,
    val method: SubmissionMethod?,
    val status: String,
    val errors: List<String>,
)
