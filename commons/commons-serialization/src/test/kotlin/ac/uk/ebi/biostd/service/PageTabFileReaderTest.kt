package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.EmptyPageTabFileException
import ac.uk.ebi.biostd.service.PageTabFileReader.readAsPageTab
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.file.ExcelReader
import ebi.ac.uk.util.file.ExcelReader.readContentAsTsv
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class PageTabFileReaderTest(
    private val tempFolder: TemporaryFolder
) {
    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() = mockkObject(ExcelReader)

    @Test
    fun `read page tab`() {
        val file = tempFolder.createFile("page-tab.tsv", "page tab")

        assertThat(readAsPageTab(file)).isEqualTo("page tab")
        verify(exactly = 0) { readContentAsTsv(file) }
    }

    @Test
    fun `read page tab from excel`() {
        val file = tempFolder.createFile("page-tab.xlsx", "page tab")

        every { readContentAsTsv(file) } returns "page tab"

        assertThat(readAsPageTab(file)).isEqualTo("page tab")
        verify(exactly = 1) { readContentAsTsv(file) }
    }

    @Test
    fun `read empty file`() {
        val file = tempFolder.createFile("page-tab.json")
        val exception = assertThrows<EmptyPageTabFileException> { readAsPageTab(file) }

        assertThat(exception.message).isEqualTo("Empty page tab file: 'page-tab.json'")
    }
}
