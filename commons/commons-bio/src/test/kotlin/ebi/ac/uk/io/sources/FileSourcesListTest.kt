package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FileFields.FILE_TYPE
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class FileSourcesListTest(
    @MockK private val oneFileSource: FilesSource,
    @MockK private val anotherFileSource: FilesSource,
    @MockK private val file: ExtFile,
) {
    private val testInstance = FileSourcesList(listOf(oneFileSource, anotherFileSource))

    private val filePath = "path/to/a/file.txt"
    private val attributes = emptyList<Attribute>()

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
}
