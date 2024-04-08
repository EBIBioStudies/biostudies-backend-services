package ebi.ac.uk.extended.model

data class WebExtPage(
    val content: List<ExtSubmission>,
    val totalElements: Long,
    val limit: Int,
    val offset: Long,
    val next: String?,
    val previous: String?,
)
