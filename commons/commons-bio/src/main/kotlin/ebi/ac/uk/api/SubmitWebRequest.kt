package ebi.ac.uk.api

import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.io.sources.PreferredSource

data class OnBehalfParameters(
    val userEmail: String,
    val userName: String?,
    val register: Boolean? = false,
) {
    companion object {
        const val ON_BEHALF_PARAM = "onBehalf"
        const val REGISTER_PARAM = "register"
        const val USER_NAME_PARAM = "name"
    }
}

data class SubmitParameters(
    val preferredSources: List<PreferredSource> = emptyList(),
    val attributes: List<SubmitAttribute> = emptyList(),
    val storageMode: StorageMode? = null,
    val silentMode: Boolean? = null,
) {
    companion object {
        const val STORAGE_MODE = "storageMode"
        const val SILENT_MODE = "silentMode"
    }
}

data class SubmitAttribute(
    val name: String,
    val value: String,
)
