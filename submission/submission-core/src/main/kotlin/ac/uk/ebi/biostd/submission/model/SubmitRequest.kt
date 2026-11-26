package ac.uk.ebi.biostd.submission.model

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ebi.ac.uk.security.integration.model.api.SecurityUser
import uk.ac.ebi.biostd.extended.model.ExtSubmission
import uk.ac.ebi.biostd.extended.model.StorageMode
import uk.ac.ebi.biostd.io.sources.FileSourcesList
import uk.ac.ebi.biostd.io.sources.PreferredSource
import uk.ac.ebi.biostd.model.Submission
import uk.ac.ebi.biostd.model.SubmissionMethod
import java.io.File

data class SubmitRequest(
    val accNo: String,
    val version: Int,
    val relPath: String,
    val submission: Submission,
    val submitter: SecurityUser,
    val owner: String,
    val sources: FileSourcesList,
    val preferredSources: List<PreferredSource>,
    val requestFiles: List<File>,
    val method: SubmissionMethod,
    val onBehalfUser: SecurityUser?,
    val collection: BasicCollection?,
    val previousVersion: ExtSubmission?,
    val storageMode: StorageMode?,
    val silentMode: Boolean,
    val singleJobMode: Boolean,
    val newSubmission: Boolean,
)
