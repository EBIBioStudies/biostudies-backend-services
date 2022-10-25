package ac.uk.ebi.pmc.persistence.docs

import ac.uk.ebi.scheduler.properties.PmcMode
import java.time.Instant

data class SubmissionErrorDoc(
    val sourceFile: String,
    val submissionText: String,
    val error: String,
    val mode: PmcMode,
    val uploaded: Instant = Instant.now(),
) {
    var accNo: String? = null

    constructor(submission: SubmissionDoc, error: String, mode: PmcMode) :
        this(submission.sourceFile, submission.body, error, mode) {
        this.accNo = submission.accNo
    }

    companion object Fields {
        const val ERROR_SOURCE_FILE = "sourceFile"
        const val ERROR_SUB_TEXT = "submissionText"
        const val ERROR_ERROR = "error"
        const val ERROR_MODE = "mode"
        const val ERROR_UPLOADED = "uploaded"
        const val ERROR_ACCNO = "accNo"
    }
}
