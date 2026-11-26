package uk.ac.ebi.biostd.extended.model

data class ExtPage(
    val content: List<ExtSubmission> = listOf(),
    val totalElements: Long,
    val next: String?,
    val previous: String?,
)
