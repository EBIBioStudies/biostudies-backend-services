package ac.uk.ebi.biostd.persistence.extensions

import DefaultSubmission.Companion.defaultSubmission
import ac.uk.ebi.biostd.persistence.filesystem.extensions.FilePermissionsExtensions.permissions
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWXR_X___
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.RW_R_____
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FilePermissionsExtensionsTest {
    @Test
    fun `permissions for public submission`() {
        val submission = defaultSubmission(released = true)
        assertThat(submission.permissions()).isEqualTo(Permissions(RW_R__R__, RWXR_XR_X))
    }

    @Test
    fun `permissions for private submission`() {
        val submission = defaultSubmission(released = false)
        assertThat(submission.permissions()).isEqualTo(Permissions(RW_R_____, RWXR_X___))
    }
}
