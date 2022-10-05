package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.Permissions
import java.io.File

sealed class FilePersistenceConfig(
    val storageMode: StorageMode
)

data class FireFilePersistenceConfig(
    val accNo: String,
    val version: Int,
    val subRelPath: String,
) : FilePersistenceConfig(FIRE)

data class NfsFilePersistenceConfig(
    val subFolder: File,
    val targetFolder: File,
    val permissions: Permissions
) : FilePersistenceConfig(NFS)
