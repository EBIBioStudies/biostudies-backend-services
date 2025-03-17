package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.InvalidPathException
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FileFields.DIRECTORY_TYPE
import ebi.ac.uk.model.constants.FileFields.FILE_TYPE
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertFailsWith

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
        fun whenOne() =
            runTest {
                coEvery { oneFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns file
                coEvery { anotherFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns null
                assertThat(testInstance.findExtFile(filePath, FILE_TYPE.value, attributes)).isEqualTo(file)
            }

        @Test
        fun whenAnother() =
            runTest {
                coEvery { oneFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns null
                coEvery { anotherFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns file
                assertThat(testInstance.findExtFile(filePath, FILE_TYPE.value, attributes)).isEqualTo(file)
            }

        @Test
        fun whenNone() =
            runTest {
                coEvery { oneFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns null
                coEvery { anotherFileSource.getExtFile(filePath, FILE_TYPE.value, attributes) } returns null
                assertThat(testInstance.findExtFile(filePath, FILE_TYPE.value, attributes)).isNull()
            }
    }

    @Nested
    inner class InvalidFilePaths {
        @Test
        fun `file with relative path`() =
            runTest {
                val error =
                    assertFailsWith<InvalidPathException> {
                        testInstance.findExtFile("./folder/file.txt", FILE_TYPE.value, attributes)
                    }
                val expectedErrorMessage =
                    """
                    The given file path contains invalid characters: ./folder/file.txt
                    For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list
                    """.trimIndent()
                assertThat(error.message).isEqualToIgnoringWhitespace(expectedErrorMessage)
            }

        @Test
        fun `file with previous folder relative path`() =
            runTest {
                val error =
                    assertFailsWith<InvalidPathException> {
                        testInstance.findExtFile("folder/../file.txt", FILE_TYPE.value, attributes)
                    }
                val expectedErrorMessage =
                    """
                    The given file path contains invalid characters: folder/../file.txt
                    For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list
                    """.trimIndent()
                assertThat(error.message).isEqualToIgnoringWhitespace(expectedErrorMessage)
            }

        @Test
        fun `file with invalid character`() =
            runTest {
                val error =
                    assertFailsWith<InvalidPathException> {
                        testInstance.findExtFile("folder/filé.txt", FILE_TYPE.value, attributes)
                    }
                val expectedErrorMessage =
                    """
                    The given file path contains invalid characters: folder/filé.txt
                    For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list
                    """.trimIndent()
                assertThat(error.message).isEqualToIgnoringWhitespace(expectedErrorMessage)
            }

        @Test
        fun `file with invalid non numeric characters`() =
            runTest {
                val error =
                    assertFailsWith<InvalidPathException> {
                        testInstance.findExtFile("Tubuline/E_KA_Exp#8_M1_12%_Tubulin_3min.Tif", FILE_TYPE.value, attributes)
                    }
                val expectedErrorMessage =
                    """
                    The given file path contains invalid characters: Tubuline/E_KA_Exp#8_M1_12%_Tubulin_3min.Tif
                    For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list
                    """.trimIndent()
                assertThat(error.message).isEqualToIgnoringWhitespace(expectedErrorMessage)
            }

        @Test
        fun `file with trailing slash`() =
            runTest {
                val error =
                    assertFailsWith<InvalidPathException> {
                        testInstance.findExtFile("folder/inner/", DIRECTORY_TYPE.value, attributes)
                    }
                val expectedErrorMessage =
                    """
                    The given file path contains invalid characters: folder/inner/
                    For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list
                    """.trimIndent()
                assertThat(error.message).isEqualToIgnoringWhitespace(expectedErrorMessage)
            }
    }

    @Nested
    inner class InvalidFileListPaths {
        @Test
        fun `file with relative path`() =
            runTest {
                val error =
                    assertFailsWith<InvalidPathException> {
                        testInstance.getFileList("./folder/file.txt")
                    }
                val expectedErrorMessage =
                    """
                    The given file path contains invalid characters: ./folder/file.txt
                    For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list
                    """.trimIndent()
                assertThat(error.message).isEqualToIgnoringWhitespace(expectedErrorMessage)
            }

        @Test
        fun `file with previous folder relative path`() =
            runTest {
                val error =
                    assertFailsWith<InvalidPathException> {
                        testInstance.getFileList("folder/../file.txt")
                    }
                val expectedErrorMessage =
                    """
                    The given file path contains invalid characters: folder/../file.txt
                    For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list
                    """.trimIndent()
                assertThat(error.message).isEqualToIgnoringWhitespace(expectedErrorMessage)
            }

        @Test
        fun `file with invalid character`() =
            runTest {
                val error =
                    assertFailsWith<InvalidPathException> {
                        testInstance.getFileList("folder/filé.txt")
                    }
                val expectedErrorMessage =
                    """
                    The given file path contains invalid characters: folder/filé.txt
                    For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list
                    """.trimIndent()
                assertThat(error.message).isEqualToIgnoringWhitespace(expectedErrorMessage)
            }

        @Test
        fun `file with invalid non numeric characters`() =
            runTest {
                val error =
                    assertFailsWith<InvalidPathException> {
                        testInstance.getFileList("Tubuline/E_KA_Exp#8_M1_12%_Tubulin_3min.Tif")
                    }
                val expectedErrorMessage =
                    """
                    The given file path contains invalid characters: Tubuline/E_KA_Exp#8_M1_12%_Tubulin_3min.Tif
                    For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list
                    """.trimIndent()
                assertThat(error.message).isEqualToIgnoringWhitespace(expectedErrorMessage)
            }

        @Test
        fun `file with trailing slash`() =
            runTest {
                val error =
                    assertFailsWith<InvalidPathException> {
                        testInstance.getFileList("folder/inner/")
                    }
                val expectedErrorMessage =
                    """
                    The given file path contains invalid characters: folder/inner/
                    For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list
                    """.trimIndent()
                assertThat(error.message).isEqualToIgnoringWhitespace(expectedErrorMessage)
            }
    }
}
