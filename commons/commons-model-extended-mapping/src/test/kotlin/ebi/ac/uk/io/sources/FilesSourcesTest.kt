package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class FilesSourcesTest(
    @MockK private val oneFileSource: FilesSource,
    @MockK private val anotherFileSource: FilesSource,
    @MockK private val file: ExtFile,
) {
    private val testInstance = FilesSources(listOf(oneFileSource, anotherFileSource))
    private val filePath = "path/to/a/file.txt"

    @Nested
    inner class GetFile {

        @Test
        fun whenOne() {
            every { oneFileSource.getExtFile(filePath) } returns file
            every { anotherFileSource.getExtFile(filePath) } returns null
            assertThat(testInstance.getExtFile(filePath)).isEqualTo(file)
        }

        @Test
        fun whenAnother() {
            every { oneFileSource.getExtFile(filePath) } returns null
            every { anotherFileSource.getExtFile(filePath) } returns file
            assertThat(testInstance.getExtFile(filePath)).isEqualTo(file)
        }

        @Test
        fun whenNone() {
            every { oneFileSource.getExtFile(filePath) } returns null
            every { anotherFileSource.getExtFile(filePath) } returns null
            assertThat(testInstance.getExtFile(filePath)).isNull()
        }
    }
}
