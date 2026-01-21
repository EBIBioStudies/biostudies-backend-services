package ebi.ac.uk.model

data class MigrateHomeOptions(
    val storageMode: String,
    val onlyIfEmptyFolder: Boolean = true,
    // Copy files modeifed in the last X days. 365 by default
    val copyFilesSinceDays: Int = 365,
)
