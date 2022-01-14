package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class InvalidFilesExceptionTest {

    @Test
    fun message() {
        val testInstance =
            InvalidFilesException("file.json", listOf(File(path = "/path/1/2"), File(path = "/path/3/4")))

        assertThat(testInstance.message).isEqualTo(
            "The following files were not found in file list file.json\n- /path/1/2\n- /path/3/4\n"
        )
    }
}
