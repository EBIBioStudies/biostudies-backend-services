package ac.uk.ebi.biostd.persistence.filesystem.extensions

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWXR_X___
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.RW_R_____
import java.nio.file.attribute.PosixFilePermission

object FilePermissionsExtensions {
    fun ExtSubmission.filePermissions(): Set<PosixFilePermission> = if (released) RW_R__R__ else RW_R_____

    fun ExtSubmission.folderPermissions(): Set<PosixFilePermission> = if (released) RWXR_XR_X else RWXR_X___
}
