package ebi.ac.uk.api

import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.io.sources.PreferredSource

data class OnBehalfParameters(
    val userEmail: String,
    val userName: String?,
    val register: Boolean?,
)

data class SubmitParameters(
    val preferredSources: List<PreferredSource> = emptyList(),
    val attributes: List<SubmitAttribute> = emptyList(),
    val storageMode: StorageMode?,
)

data class SubmitAttribute(
    val name: String,
    val value: String,
)
