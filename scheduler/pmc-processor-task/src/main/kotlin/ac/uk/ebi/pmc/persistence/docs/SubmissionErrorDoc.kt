package ac.uk.ebi.pmc.persistence.docs

import java.time.Instant

data class SubmissionErrorDoc(
    val submissionText: String,
    val sourceFile: String,
    val error: String,
    val uploaded: Instant = Instant.now()
) {
    var accNo: String? = null

    constructor(submission: SubmissionDoc, error: String) :
        this(submission.body, submission.sourceFile, error) {
        this.accNo = submission.accNo
    }
}

data class PlainSubmissionErrorDoc(
    val sourceFile: String,
    val submissionBody: String,
    val error: String,
    val uploaded: Instant = Instant.now())
