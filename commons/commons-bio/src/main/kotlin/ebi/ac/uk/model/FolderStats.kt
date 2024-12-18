package ebi.ac.uk.model

import java.time.Instant

data class FolderStats(
    val totalFiles: Int,
    val totalDirectories: Int,
    val totalFilesSize: Long,
    val lastModification: Instant,
)
