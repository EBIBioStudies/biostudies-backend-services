package ac.uk.ebi.pmc.persistence.docs

import ac.uk.ebi.scheduler.properties.PmcMode
import java.time.Instant

data class SubmissionErrorDoc(
    val sourceFile: String,
    val submissionText: String,
    val error: String,
    val mode: PmcMode,
    val uploaded: Instant = Instant.now()
) {
    var accNo: String? = null

    constructor(submission: SubmissionDoc, error: String, mode: PmcMode) :
        this(submission.sourceFile, submission.body, error, mode) {
        this.accNo = submission.accno
    }
}
