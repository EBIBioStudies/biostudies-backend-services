package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.io.sources.FileSourcesList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.io.sources.PathSource
import kotlin.io.path.Path

internal class FilesProcessingExceptionTest {
    private val invalidFiles = listOf("file1.txt", "file2.txt")
    private val sources = listOf(
        PathSource("User jhonDoe@ebi.ac.uk files", Path("a/path")),
        PathSource("Ebi Research Group files", Path("a/path")),
    )

    @Test
    fun message() {
        val testInstance = FilesProcessingException(invalidFiles, FileSourcesList(sources))

        assertThat(testInstance.message).isEqualTo(
            """
            The following files could not be found:
              - file1.txt
              - file2.txt
            List of available sources:
              - User jhonDoe@ebi.ac.uk files
              - Ebi Research Group files
            """.trimIndent()
        )
    }
}
