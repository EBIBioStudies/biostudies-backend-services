package ac.uk.ebi.biostd.persistence.common.request

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode

data class SubmissionRequest(
    val submission: ExtSubmission,
    val fileMode: FileMode,
    val draftKey: String? = null,
)

data class ProcessedSubmissionRequest(
    val submission: ExtSubmission,
    val fileMode: FileMode,
    val draftKey: String? = null,
    val previousVersion: ExtSubmission?,
)
