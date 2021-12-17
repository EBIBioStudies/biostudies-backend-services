package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.XlsxTsv
import ac.uk.ebi.biostd.service.PageTabFileReader.readAsPageTab
import ac.uk.ebi.biostd.validation.InvalidChunkSizeException
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
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
        every { serializer.deserializeFileList(fileList, JSON) } returns filesTable

        testInstance.deserializeFileList(submission, source)

        assertSubmissionFileList(submission, fileListName)
    }

    @Test
    fun `deserialize TSV file list`() {
        val fileListName = "FileList.tsv"
        val submission = testSubmission(fileListName)
        val fileList = tempFolder.createFile(fileListName, "test file list")

        every { readAsPageTab(fileList) } returns "test file list"
        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeFileList(fileList, TSV) } returns filesTable

        testInstance.deserializeFileList(submission, source)

        assertSubmissionFileList(submission, fileListName)
    }

    @Test
    fun `deserialize XML file list`() {
        val fileListName = "FileList.xml"
        val submission = testSubmission(fileListName)
        val fileList = tempFolder.createFile(fileListName, "test file list")

        every { readAsPageTab(fileList) } returns "test file list"
        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeFileList(fileList, XML) } returns filesTable

        testInstance.deserializeFileList(submission, source)

        assertSubmissionFileList(submission, fileListName)
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
        every { serializer.deserializeFileList(fileList, XlsxTsv) } returns filesTable

        testInstance.deserializeFileList(submission, source)

        assertSubmissionFileList(submission, fileListName)
    }

    @Test
    fun `deserialize standalone file list`() {
        val fileListName = "AFileList.tsv"
        val fileList = tempFolder.createFile(fileListName, "test file list")

        every { readAsPageTab(fileList) } returns "test file list"
        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeFileList(fileList, TSV) } returns filesTable

        assertFileList(testInstance.deserializeFileList(fileListName, source), fileListName)
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

    @Test
    fun `deserialize file list with invalid page tab`() {
        val fileListName = "BFileList.tsv"
        val fileList = tempFolder.createFile(fileListName, "test file list")

        every { readAsPageTab(fileList) } returns "test file list"
        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeFileList(fileList, TSV) } throws InvalidChunkSizeException()

        val exception = assertThrows<InvalidFileListException> {
            testInstance.deserializeFileList(fileListName, source)
        }
        assertThat(exception.message).isEqualTo(
            "Problem processing file list 'BFileList.tsv': The provided page tab doesn't match the file list format"
        )
    }

    @Test
    fun `deserialize file list with a valid page tab but NOT file list element`() {
        val fileListName = "CFileList.tsv"
        val fileList = tempFolder.createFile(fileListName, "test file list")

        every { readAsPageTab(fileList) } returns "test file list"
        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeFileList(fileList, TSV) } throws ClassCastException()

        val exception = assertThrows<InvalidFileListException> {
            testInstance.deserializeFileList(fileListName, source)
        }
        assertThat(exception.message).isEqualTo(
            "Problem processing file list 'CFileList.tsv': The provided page tab doesn't match the file list format"
        )
    }

    private fun testSubmission(fileList: String) = submission("S-TEST123") {
        section("Study") {
            attribute("File List", fileList)
        }
    }

    private fun assertSubmissionFileList(submission: Submission, fileListName: String) {
        assertNotNull(submission.section.fileList)
        assertFileList(submission.section.fileList!!, fileListName)
    }

    private fun assertFileList(fileList: FileList, fileListName: String) {
        assertThat(fileList.name).isEqualTo(fileListName)
        assertThat(fileList.referencedFiles).isEqualTo(listOf(File("some-file.txt")))
    }
}
