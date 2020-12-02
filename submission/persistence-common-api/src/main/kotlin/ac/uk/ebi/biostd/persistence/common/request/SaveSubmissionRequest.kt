package ac.uk.ebi.biostd.persistence.common.request

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode

data class SaveSubmissionRequest(
    val submission: ExtSubmission,
    val fileMode: FileMode
) {
    operator fun component3() = submission.accNo
}
