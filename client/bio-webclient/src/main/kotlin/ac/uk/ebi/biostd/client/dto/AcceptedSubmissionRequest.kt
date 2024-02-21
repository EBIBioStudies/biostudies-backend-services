package ac.uk.ebi.biostd.client.dto

data class AcceptedSubmissionRequest(
    val accNo: String,
    val version: Int,
)