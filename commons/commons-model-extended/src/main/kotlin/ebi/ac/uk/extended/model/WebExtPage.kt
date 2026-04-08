package ebi.ac.uk.extended.model

data class WebExtPage<T>(
    val content: List<T>,
    val totalElements: Long,
    val limit: Int,
    val offset: Long,
    val next: String?,
    val previous: String?,
)

data class FileExtPage(
    val content: List<ExtFile>,
    val totalElements: Long,
    val limit: Int,
    val offset: Long,
    val next: String?,
    val previous: String?,
)
