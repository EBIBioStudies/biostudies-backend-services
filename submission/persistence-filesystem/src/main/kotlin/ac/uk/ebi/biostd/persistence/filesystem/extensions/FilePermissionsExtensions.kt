package ac.uk.ebi.biostd.persistence.filesystem.extensions

import ebi.ac.uk.extended.model.ExtSubmissionInfo
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWXR_X__X
import ebi.ac.uk.io.RW_R__R__

fun ExtSubmissionInfo.permissions(): Permissions {
    return if (released) Permissions(RW_R__R__, RWXR_XR_X) else Permissions(RW_R__R__, RWXR_X__X)
}
