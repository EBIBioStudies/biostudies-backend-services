package ac.uk.ebi.biostd.persistence.common.request

import ebi.ac.uk.extended.model.ExtSubmission

data class ExtSubmitRequest(
    val submission: ExtSubmission,
    val notifyTo: String,
    val draftKey: String? = null,
    val silentMode: Boolean = false,
    val singleJobMode: Boolean = true,
)
