package ac.uk.ebi.biostd.persistence.common.filesystem

import ebi.ac.uk.extended.model.ExtFile
import java.io.File
import java.nio.file.attribute.PosixFilePermission

data class FileProcessRequest(
    val subFolder: File,
    val tempFolder: File,
    val filePermissions: Set<PosixFilePermission>,
    val folderPermissions: Set<PosixFilePermission>,
    val processFunction: (ExtFile, File, File, Set<PosixFilePermission>, Set<PosixFilePermission>) -> Unit
)
