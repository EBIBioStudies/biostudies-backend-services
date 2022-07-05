package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.security.integration.model.api.SecurityUser

data class SubmitRequest(
    val submission: Submission,
    val submitter: SecurityUser,
    val sources: FileSourcesList,
    val method: SubmissionMethod,
    val mode: FileMode,
    val onBehalfUser: SecurityUser? = null,
    val draftKey: String? = null,
) {
    val accNo: String = submission.accNo.ifBlank { "PENDING_ACC_NO" }

    val owner: String = onBehalfUser?.email ?: submitter.email
}
