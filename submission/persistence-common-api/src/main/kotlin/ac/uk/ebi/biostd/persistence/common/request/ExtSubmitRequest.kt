package ac.uk.ebi.biostd.persistence.common.request

import ebi.ac.uk.extended.model.ExtSubmission

// TODO add the owner
data class ExtSubmitRequest(
    val submission: ExtSubmission,
    val notifyTo: String,
    // TODO this should be just key
    val draftKey: String,
    val draftContent: String,
    val silentMode: Boolean = false,
    val singleJobMode: Boolean = true,
)
