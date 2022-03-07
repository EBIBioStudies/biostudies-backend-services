package ebi.ac.uk.extended.model

data class ExtPage(
    val content: List<ExtSubmission> = listOf(),
    val totalElements: Long,
    val next: String?,
    val previous: String?
)
