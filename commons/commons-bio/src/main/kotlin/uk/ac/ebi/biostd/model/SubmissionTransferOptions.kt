package uk.ac.ebi.biostd.model

data class SubmissionTransferOptions(
    val owner: String,
    val newOwner: String,
    val userName: String? = null,
    val accNoList: List<String> = emptyList(),
)
