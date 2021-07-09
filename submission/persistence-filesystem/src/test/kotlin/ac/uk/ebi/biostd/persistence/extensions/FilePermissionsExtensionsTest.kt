package ac.uk.ebi.biostd.persistence.extensions

import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.filePermissions
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.folderPermissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWXR_X___
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.RW_R_____
import ebi.ac.uk.test.basicExtSubmission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FilePermissionsExtensionsTest {
    @Test
    fun `permissions for public submission`() {
        val submission = basicExtSubmission.copy(released = true)
        assertThat(submission.filePermissions()).isEqualTo(RW_R__R__)
        assertThat(submission.folderPermissions()).isEqualTo(RWXR_XR_X)
    }

    @Test
    fun `permissions for private submission`() {
        val submission = basicExtSubmission.copy(released = false)
        assertThat(submission.filePermissions()).isEqualTo(RW_R_____)
        assertThat(submission.folderPermissions()).isEqualTo(RWXR_X___)
    }
}
