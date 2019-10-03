package ac.uk.ebi.biostd.submission.web.handlers

import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.file.ExcelReader
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class PageTabReaderTest(@MockK private val excelReader: ExcelReader, temporaryFolder: TemporaryFolder) {
    private val testInstance = PageTabReader(excelReader)
    private val excelFile = temporaryFolder.createFile("excelFile.xlsx")
    private val textFile = temporaryFolder.createFile("textFile.txt", "a text")

    @BeforeEach
    fun beforeEach() {
        every { excelReader.readContentAsTsv(excelFile) } returns "excel text"
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `read excel file`() {
        assertThat(testInstance.read(excelFile)).isEqualTo("excel text")
        verify(exactly = 1) { excelReader.readContentAsTsv(excelFile) }
    }

    @Test
    fun `read text file`() {
        assertThat(testInstance.read(textFile)).isEqualTo("a text")
        verify(exactly = 0) { excelReader.readContentAsTsv(textFile) }
    }
}
