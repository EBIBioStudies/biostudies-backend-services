package ebi.ac.uk.model

data class SubmissionTransferOptions(
    val owner: String,
    val newOwner: String,
    val accNoList: List<String> = emptyList(),
)
