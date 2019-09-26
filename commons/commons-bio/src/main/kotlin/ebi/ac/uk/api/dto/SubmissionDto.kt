package ebi.ac.uk.api.dto

data class SubmissionDto(
    val accno: String,
    val title: String?,
    val ctime: Long,
    val mtime: Long,
    val rtime: Long
)
