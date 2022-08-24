package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.security.integration.model.api.SecurityUser

data class SubmitRequest(
    val submission: Submission,
    val submitter: SecurityUser,
    val sources: FileSourcesList,
    val method: SubmissionMethod,
    val onBehalfUser: SecurityUser?,
    val draftKey: String?,
    val previousVersion: ExtSubmission?,
    val storageMode: StorageMode?,
) {
    val accNo: String = submission.accNo.ifBlank { "PENDING_ACC_NO" }
    val owner: String = onBehalfUser?.email ?: submitter.email
}
