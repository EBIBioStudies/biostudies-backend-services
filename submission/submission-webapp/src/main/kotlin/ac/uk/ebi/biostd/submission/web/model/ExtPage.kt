package ac.uk.ebi.biostd.submission.web.model

import ebi.ac.uk.extended.model.ExtSubmission

data class ExtPage(
    val content: List<ExtSubmission>,
    val totalRecords: Int,
    val nextPage: String?,
    val previousPage: String?
)
