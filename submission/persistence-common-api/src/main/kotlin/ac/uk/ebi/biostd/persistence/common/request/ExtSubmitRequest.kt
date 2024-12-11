package ac.uk.ebi.biostd.persistence.common.request

import ebi.ac.uk.extended.model.ExtSubmission

data class ExtSubmitRequest(
    val key: String? = null,
    val owner: String,
    val notifyTo: String,
    val submission: ExtSubmission,
    val silentMode: Boolean = false,
    val singleJobMode: Boolean = true,
)
