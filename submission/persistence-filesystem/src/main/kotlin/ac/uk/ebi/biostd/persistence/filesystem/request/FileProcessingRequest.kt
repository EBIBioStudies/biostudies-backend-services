package ac.uk.ebi.biostd.persistence.filesystem.request

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import java.io.File
import java.nio.file.attribute.PosixFilePermission

data class FileProcessingRequest(
    val mode: FileMode,
    val submission: ExtSubmission,
    val config: FileProcessingConfig
)

data class FileProcessingConfig(
    val subFolder: File,
    val tempFolder: File,
    val filePermissions: Set<PosixFilePermission>,
    val dirPermissions: Set<PosixFilePermission>
)
