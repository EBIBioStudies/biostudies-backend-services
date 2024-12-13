package ac.uk.ebi.biostd.submission.model

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.security.integration.model.api.SecurityUser

data class SubmitRequest(
    val key: String,
    val submission: Submission,
    val submitter: SecurityUser,
    val sources: FileSourcesList,
    val method: SubmissionMethod,
    val onBehalfUser: SecurityUser?,
    val collection: BasicCollection?,
    val previousVersion: ExtSubmission?,
    val storageMode: StorageMode?,
    val silentMode: Boolean,
    val singleJobMode: Boolean,
) {
    val accNo: String = submission.accNo.ifBlank { "PENDING_ACC_NO" }
    val owner: String = onBehalfUser?.email ?: submitter.email
}
