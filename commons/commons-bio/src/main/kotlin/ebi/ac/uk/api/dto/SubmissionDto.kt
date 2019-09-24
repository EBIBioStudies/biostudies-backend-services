package ebi.ac.uk.api.dto

data class SubmissionDto (
    var accno: String,
    var title: String?,
    var ctime: Long,
    var mtime: Long,
    var rtime: Long
)

