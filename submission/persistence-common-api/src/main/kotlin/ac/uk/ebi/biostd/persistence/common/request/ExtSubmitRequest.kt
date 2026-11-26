package ac.uk.ebi.biostd.persistence.common.request

import uk.ac.ebi.biostd.extended.model.ExtSubmission
import uk.ac.ebi.biostd.io.sources.PreferredSource
import java.io.File

data class ExtSubmitRequest(
    val owner: String,
    val submission: ExtSubmission,
    val previousVersion: Int? = null,
    val onBehalfUser: String? = null,
    val newSubmission: Boolean,
    val preferredSources: List<PreferredSource> = listOf(PreferredSource.SUBMISSION),
    val requestFiles: List<File> = emptyList(),
    val silentMode: Boolean = false,
    val singleJobMode: Boolean = true,
)
