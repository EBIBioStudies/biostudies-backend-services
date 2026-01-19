package ebi.ac.uk.model

import java.time.Instant

data class FolderStats(
    val totalFiles: Int,
    val totalDirectories: Int,
    val totalFilesSize: Long,
    val lastModification: Instant,
)

data class FolderInventory(val files: List<InventoryFile>)

val FolderInventory.nonSubmissionFiles
    get() : Int = files.count() { it.submission == null }

data class InventoryFile(val path: String, val size: Long, val md5: String, val submission: String?)
