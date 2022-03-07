package ac.uk.ebi.biostd.submission.model

data class ReleaseRequest(
    val accNo: String,
    val owner: String,
    val relPath: String
)
