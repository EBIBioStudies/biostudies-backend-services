package ac.uk.ebi.biostd.submission.web.model

import ebi.ac.uk.extended.model.ExtSubmission

data class ExtPage(
    val content: List<ExtSubmission>,
    val totalElements: Long,
    val limit: Int,
    val offset: Long,
    val next: String?,
    val previous: String?
)
