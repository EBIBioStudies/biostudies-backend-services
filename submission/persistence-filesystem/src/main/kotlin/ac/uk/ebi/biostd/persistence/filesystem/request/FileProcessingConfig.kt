package ac.uk.ebi.biostd.persistence.filesystem.request

import java.io.File
import java.nio.file.attribute.PosixFilePermission

data class FileProcessingConfig(
    val subFolder: File,
    val tempFolder: File,
    val filePermissions: Set<PosixFilePermission>,
    val dirPermissions: Set<PosixFilePermission>
)
