package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.XlsxTsv
import ac.uk.ebi.biostd.service.PageTabFileReader.readAsPageTab
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.file.ExcelReader
import ebi.ac.uk.util.file.ExcelReader.readContentAsTsv
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertNotNull

@ExtendWith(TemporaryFolderExtension::class)
class FileListSerializerTest(private val tempFolder: TemporaryFolder) {
    private val source = mockk<FilesSource>()
    private val serializer = mockk<PagetabSerializer>()
    private val filesTable = filesTable { file("some-file.txt") }
    private val testInstance = FileListSerializer(serializer)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() = mockkObject(PageTabFileReader)

    @Test
    fun `deserialize JSON file list`() {
        val fileListName = "FileList.json"
        val submission = testSubmission(fileListName)
        val fileList = tempFolder.createFile(fileListName, "test file list")

        every { readAsPageTab(fileList) } returns "test file list"
        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeElement<FilesTable>("test file list", JSON) } returns filesTable

        testInstance.deserializeFileList(submission, source)

        assertFileList(submission, fileListName)
    }

    @Test
    fun `deserialize TSV file list`() {
        val fileListName = "FileList.tsv"
        val submission = testSubmission(fileListName)
        val fileList = tempFolder.createFile(fileListName, "test file list")

        every { readAsPageTab(fileList) } returns "test file list"
        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeElement<FilesTable>("test file list", TSV) } returns filesTable

        testInstance.deserializeFileList(submission, source)

        assertFileList(submission, fileListName)
    }

    @Test
    fun `deserialize XML file list`() {
        val fileListName = "FileList.xml"
        val submission = testSubmission(fileListName)
        val fileList = tempFolder.createFile(fileListName, "test file list")

        every { readAsPageTab(fileList) } returns "test file list"
        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeElement<FilesTable>("test file list", XML) } returns filesTable

        testInstance.deserializeFileList(submission, source)

        assertFileList(submission, fileListName)
    }

    @Test
    fun `deserialize XLS file list`() {
        val fileListName = "FileList.xlsx"
        val submission = testSubmission(fileListName)
        val fileList = tempFolder.createFile(fileListName, "test file list")

        mockkObject(ExcelReader)
        every { readAsPageTab(fileList) } returns "test file list"
        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { readContentAsTsv(fileList) } returns "test file list"
        every { serializer.deserializeElement<FilesTable>("test file list", XlsxTsv) } returns filesTable

        testInstance.deserializeFileList(submission, source)

        assertFileList(submission, fileListName)
    }

    @Test
    fun `deserialize unsupported file list format`() {
        val fileListName = "FileList.txt"
        val submission = testSubmission(fileListName)
        val fileList = tempFolder.createFile(fileListName)

        every { readAsPageTab(fileList) } returns "test file list"
        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        val exception = assertThrows<InvalidFileListException> { testInstance.deserializeFileList(submission, source) }

        assertThat(exception.message).isEqualTo(
            "Problem processing file list 'FileList.txt': Unsupported page tab format FileList.txt"
        )
    }

    private fun testSubmission(fileList: String) = submission("S-TEST123") {
        section("Study") {
            attribute("File List", fileList)
        }
    }

    private fun assertFileList(submission: Submission, fileListName: String) {
        assertNotNull(submission.section.fileList)
        assertThat(submission.section.fileList!!.name).isEqualTo(fileListName)
        assertThat(submission.section.fileList!!.referencedFiles).isEqualTo(listOf(File("some-file.txt")))
    }
}
