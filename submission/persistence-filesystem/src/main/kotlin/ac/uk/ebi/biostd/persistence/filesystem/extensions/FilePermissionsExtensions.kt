package ac.uk.ebi.biostd.persistence.filesystem.extensions

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWXR_X___
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.RW_R_____

object FilePermissionsExtensions {
    fun ExtSubmission.permissions(): Permissions = if (released) Permissions(RW_R__R__, RWXR_XR_X) else Permissions(RW_R_____, RWXR_X___)
}
