package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.sources.FilesSource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class FilesProcessingExceptionTest(
    @MockK private val userSource: FilesSource,
    @MockK private val groupSource: FilesSource,
) {
    private val invalidFiles = listOf("file1.txt", "file2.txt")

    @BeforeEach
    fun beforeEach() {
        every { userSource.description } returns "User jhonDoe@ebi.ac.uk files"
        every { groupSource.description } returns "Ebi Research Group files"
    }

    @Test
    fun message() {
        val testInstance = FilesProcessingException(invalidFiles, FileSourcesList(listOf(userSource, groupSource)))

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
