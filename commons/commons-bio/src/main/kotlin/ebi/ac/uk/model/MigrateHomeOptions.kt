package ebi.ac.uk.model

data class MigrateHomeOptions(
    val storageMode: String,
    val onlyIfEmptyFolder: Boolean = true,
)
