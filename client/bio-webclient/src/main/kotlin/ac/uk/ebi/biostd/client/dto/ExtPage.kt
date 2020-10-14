package ac.uk.ebi.biostd.client.dto

import ebi.ac.uk.extended.model.ExtSubmission

data class ExtPage(
    val content: List<ExtSubmission>,
    val totalElements: Long,
    val next: String?,
    val previous: String?
)
