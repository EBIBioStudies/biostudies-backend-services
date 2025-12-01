package ac.uk.ebi.biostd.persistence.common.request

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.PreferredSource
import java.io.File

data class ExtSubmitRequest(
    val notifyTo: String,
    val submission: ExtSubmission,
    val previousVersion: Int? = null,
    val onBehalfUser: String? = null,
    val newSubmission: Boolean,
    val preferredSources: List<PreferredSource> = listOf(PreferredSource.SUBMISSION),
    val requestFiles: List<File> = emptyList(),
    val silentMode: Boolean = false,
    val singleJobMode: Boolean = true,
)
