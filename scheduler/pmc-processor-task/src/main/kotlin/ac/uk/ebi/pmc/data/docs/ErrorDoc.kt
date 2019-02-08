package ac.uk.ebi.pmc.data.docs

import java.time.Instant

data class ErrorDoc(
    val id: String,
    val body: String,
    val sourceFile: String,
    val error: String,
    val uploaded: Instant = Instant.now()
) {
    constructor(submission: SubmissionDoc, error: String) :
        this(submission.id, submission.body, submission.sourceFile, error)
}
