package ac.uk.ebi.biostd.submission.model

import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

data class SubmissionConfig(
    val submitter: SecurityUser,
    val onBehalfUser: SecurityUser?,
    val attrs: List<ExtAttributeDetail>,
    val storageMode: StorageMode?,
    val silentMode: Boolean,
    val singleJobMode: Boolean,
)

data class SubmissionFilesConfig(
    val files: List<File>?,
    val preferredSources: List<PreferredSource>,
)

sealed class SubmitWebRequest(
    val config: SubmissionConfig,
    val filesConfig: SubmissionFilesConfig,
)

val SubmitWebRequest.method: SubmissionMethod
    get() =
        when (this) {
            is ContentSubmitWebRequest -> SubmissionMethod.PAGE_TAB
            is FileSubmitWebRequest -> SubmissionMethod.FILE
        }

val SubmitWebRequest.draftKey: String?
    get() =
        when (this) {
            is ContentSubmitWebRequest -> draftKey
            is FileSubmitWebRequest -> null
        }

val SubmitWebRequest.draftContent: String?
    get() =
        when (this) {
            is ContentSubmitWebRequest -> draftContent
            is FileSubmitWebRequest -> null
        }

class ContentSubmitWebRequest(
    val submission: String,
    val draftKey: String? = null,
    val draftContent: String? = null,
    val format: SubFormat,
    submissionConfig: SubmissionConfig,
    filesConfig: SubmissionFilesConfig,
) : SubmitWebRequest(submissionConfig, filesConfig)

class FileSubmitWebRequest(
    val submission: File,
    submissionConfig: SubmissionConfig,
    filesConfig: SubmissionFilesConfig,
) : SubmitWebRequest(submissionConfig, filesConfig)
