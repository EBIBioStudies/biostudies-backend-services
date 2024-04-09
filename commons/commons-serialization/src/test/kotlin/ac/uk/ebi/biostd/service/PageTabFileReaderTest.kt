package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.EmptyPageTabFileException
import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.service.PageTabFileReader.getFileListFile
import ac.uk.ebi.biostd.service.PageTabFileReader.readAsPageTab
import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.file.ExcelReader
import ebi.ac.uk.util.file.ExcelReader.asTsv
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class PageTabFileReaderTest(
    private val tempFolder: TemporaryFolder,
) {
    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() = mockkObject(ExcelReader)

    @Test
    fun `read page tab`() {
        val file = tempFolder.createFile("page-tab.tsv", "page tab")

        assertThat(readAsPageTab(file)).isEqualTo(file)
        verify(exactly = 0) { asTsv(file) }
    }

    @Test
    fun `read page tab from excel`() {
        val file = tempFolder.createFile("page-tab.xlsx", "page tab")

        every { asTsv(file) } returns file

        assertThat(readAsPageTab(file)).isEqualTo(file)
        verify(exactly = 1) { asTsv(file) }
    }

    @Test
    fun `read empty file`() {
        val file = tempFolder.createFile("page-tab.json")
        val exception = assertThrows<EmptyPageTabFileException> { readAsPageTab(file) }

        assertThat(exception.message).isEqualTo("Empty page tab file: 'page-tab.json'")
    }

    @Test
    fun `get file list file`(
        @MockK filesSource: FilesSource,
    ) = runTest {
        val filesSourceList = FileSourcesList(true, listOf(filesSource))
        val fileList = tempFolder.createFile("file-list.tsv")

        coEvery { filesSource.getFileList("file-list.tsv") } returns fileList

        assertThat(getFileListFile("file-list.tsv", filesSourceList)).isEqualTo(fileList)
    }

    @Test
    fun `get xlsx file list file`(
        @MockK filesSource: FilesSource,
    ) = runTest {
        val filesSourceList = FileSourcesList(true, listOf(filesSource))
        val fileList = tempFolder.createFile("file-list.xlsx")
        val tsvFileList = tempFolder.createFile("converted-file-list.tsv")

        every { asTsv(fileList) } returns fileList
        coEvery { filesSource.getFileList("file-list.xlsx") } returns tsvFileList

        assertThat(getFileListFile("file-list.xlsx", filesSourceList)).isEqualTo(tsvFileList)
    }

    @Test
    fun `get directory list file`(
        @MockK filesSource: FilesSource,
    ) = runTest {
        val filesSourceList = FileSourcesList(true, listOf(filesSource))
        val fileList = tempFolder.createDirectory("file-list")

        coEvery { filesSource.getFileList("file-list") } returns fileList

        val exception = assertFailsWith<InvalidFileListException> { getFileListFile("file-list", filesSourceList) }
        assertThat(exception.message)
            .isEqualTo("Problem processing file list 'file-list': A directory can't be used as File List")
    }

    @Test
    fun `file list not found`(
        @MockK filesSource: FilesSource,
    ) = runTest {
        val filesSourceList = FileSourcesList(true, listOf(filesSource))
        coEvery { filesSource.getFileList("file-list.xml") } returns null

        assertFailsWith<FilesProcessingException> { getFileListFile("file-list.xml", filesSourceList) }
    }
}
