package ac.uk.ebi.biostd.persistence.filesystem.extensions

import ebi.ac.uk.extended.model.ExtSubmissionInfo
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWXR_X__X
import ebi.ac.uk.io.RW_R__R__

/**
 * Provide the expected permissions for the submission files based on its release status.
 *
 * - Public Submissions: All files should be readable. Directories should be readable and listable
 * - Private Submissions: All files should be readable. Directories should be readable but not listable
 */
fun ExtSubmissionInfo.permissions(): Permissions =
    when {
        released -> Permissions(file = RW_R__R__, parentsFolder = RWXR_XR_X, subFolder = RWXR_XR_X)
        else -> Permissions(file = RW_R__R__, parentsFolder = RWXR_X__X, subFolder = RWXR_XR_X)
    }
