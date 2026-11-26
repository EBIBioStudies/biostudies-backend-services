package uk.ac.ebi.biostd.model

import java.time.Instant

data class FolderStats(
    val totalFiles: Int,
    val totalDirectories: Int,
    val totalFilesSize: Long,
    val lastModification: Instant,
)
