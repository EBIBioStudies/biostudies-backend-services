package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.InvalidPathException
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FileFields.DIRECTORY_TYPE
import ebi.ac.uk.model.constants.FileFields.FILE_TYPE
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class FileSourcesListTest(
    @MockK private val file: ExtFile,
    @MockK private val oneFileSource: FilesSource,
    @MockK private val anotherFileSource: FilesSource,
) {
    private val testInstance = FileSourcesList(listOf(oneFileSource, anotherFileSource))

    private val filePath = "path/to/a/my file.txt"
    private val attributes = emptyList<Attribute>()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Nested
    inner class GetFile {
        @Test
        fun whenOne() {
            every { oneFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns file
            every { anotherFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns null
            assertThat(testInstance.findExtFile(filePath, FILE_TYPE.value, attributes)).isEqualTo(file)
        }

        @Test
        fun whenAnother() {
            every { oneFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns null
            every { anotherFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns file
            assertThat(testInstance.findExtFile(filePath, FILE_TYPE.value, attributes)).isEqualTo(file)
        }

        @Test
        fun whenNone() {
            every { oneFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns null
            every { anotherFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns null
            assertThat(testInstance.findExtFile(filePath, FILE_TYPE.value, attributes)).isNull()
        }
    }

    @Nested
    inner class InvalidPaths {
        @Test
        fun `file with relative path`() {
            val error = assertThrows<InvalidPathException> {
                testInstance.findExtFile("./folder/file.txt", FILE_TYPE.value, attributes)
            }
            assertThat(error.message).isEqualTo("The given file path contains invalid characters: ./folder/file.txt")
        }

        @Test
        fun `file with previous folder relative path`() {
            val error = assertThrows<InvalidPathException> {
                testInstance.findExtFile("folder/../file.txt", FILE_TYPE.value, attributes)
            }
            assertThat(error.message).isEqualTo("The given file path contains invalid characters: folder/../file.txt")
        }

        @Test
        fun `file with invalid character`() {
            val error = assertThrows<InvalidPathException> {
                testInstance.findExtFile("folder/filé.txt", FILE_TYPE.value, attributes)
            }
            assertThat(error.message).isEqualTo("The given file path contains invalid characters: folder/filé.txt")
        }

        @Test
        fun `file with trailing slash`() {
            val error = assertThrows<InvalidPathException> {
                testInstance.findExtFile("folder/inner/", DIRECTORY_TYPE.value, attributes)
            }
            assertThat(error.message).isEqualTo("The given file path contains invalid characters: folder/inner/")
        }
    }
}
