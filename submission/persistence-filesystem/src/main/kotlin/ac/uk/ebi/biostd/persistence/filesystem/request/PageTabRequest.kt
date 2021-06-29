package ac.uk.ebi.biostd.persistence.filesystem.request

import ebi.ac.uk.extended.model.ExtSubmission
import java.io.File
import java.nio.file.attribute.PosixFilePermission

data class PageTabRequest(
    val submission: ExtSubmission,
    val submissionFolder: File,
    val filePermissions: Set<PosixFilePermission>,
    val folderPermissions: Set<PosixFilePermission>
)
