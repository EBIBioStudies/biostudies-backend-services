package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.XlsxTsv
import ac.uk.ebi.biostd.validation.InvalidChunkSizeException
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.file.ExcelReader
import ebi.ac.uk.util.file.ExcelReader.asTsv
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.io.InputStream
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
        val inputStream = slot<InputStream>()

        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeFileList(capture(inputStream), JSON) } returns sequenceOf(file("some-file.txt"))

        testInstance.deserializeFileList(submission, source)

        assertSubmissionFileList(submission, fileListName)
        verify(exactly = 1) { serializer.deserializeFileList(inputStream.captured, JSON) }
    }

    @Test
    fun `deserialize TSV file list`() {
        val fileListName = "FileList.tsv"
        val submission = testSubmission(fileListName)
        val fileList = tempFolder.createFile(fileListName, "test file list")
        val inputStream = slot<InputStream>()

        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeFileList(capture(inputStream), TSV) } returns sequenceOf(file("some-file.txt"))

        testInstance.deserializeFileList(submission, source)

        assertSubmissionFileList(submission, fileListName)
        verify(exactly = 1) { serializer.deserializeFileList(inputStream.captured, TSV) }
    }

    @Test
    fun `deserialize XML file list`() {
        val fileListName = "FileList.xml"
        val submission = testSubmission(fileListName)
        val fileList = tempFolder.createFile(fileListName, "test file list")
        val inputStream = slot<InputStream>()

        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeFileList(capture(inputStream), XML) } returns sequenceOf(file("some-file.txt"))

        testInstance.deserializeFileList(submission, source)

        assertSubmissionFileList(submission, fileListName)
        verify(exactly = 1) { serializer.deserializeFileList(inputStream.captured, XML) }
    }

    @Test
    fun `deserialize XLS file list`() {
        val fileListName = "FileList.xlsx"
        val submission = testSubmission(fileListName)
        val fileList = tempFolder.createFile(fileListName, "test file list")
        val inputStream = slot<InputStream>()

        val tempFile = java.io.File.createTempFile("file", "temp")
        tempFile.writeText("test file list")

        mockkObject(ExcelReader)
        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { asTsv(fileList) } returns tempFile
        every {
            serializer.deserializeFileList(
                capture(inputStream),
                XlsxTsv
            )
        } returns sequenceOf(file("some-file.txt"))

        testInstance.deserializeFileList(submission, source)

        assertSubmissionFileList(submission, fileListName)
        verify(exactly = 1) { serializer.deserializeFileList(inputStream.captured, XlsxTsv) }
    }

    @Test
    fun `deserialize standalone file list`() {
        val fileListName = "AFileList.tsv"
        val fileList = tempFolder.createFile(fileListName, "test file list")
        val inputStream = slot<InputStream>()

        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeFileList(capture(inputStream), TSV) } returns sequenceOf(file("some-file.txt"))

        assertFileList(testInstance.deserializeFileList(fileListName, source), fileListName)
        verify(exactly = 1) { serializer.deserializeFileList(inputStream.captured, TSV) }
    }

    @Test
    fun `deserialize unsupported file list format`() {
        val fileListName = "FileList.txt"
        val submission = testSubmission(fileListName)
        val fileList = tempFolder.createFile(fileListName)

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
        val inputStream = slot<InputStream>()

        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeFileList(capture(inputStream), TSV) } throws InvalidChunkSizeException()

        val exception = assertThrows<InvalidFileListException> {
            testInstance.deserializeFileList(fileListName, source)
        }
        assertThat(exception.message).isEqualTo(
            "Problem processing file list 'BFileList.tsv': The provided page tab doesn't match the file list format"
        )
        verify(exactly = 1) { serializer.deserializeFileList(inputStream.captured, TSV) }
    }

    @Test
    fun `deserialize file list with a valid page tab but NOT file list element`() {
        val fileListName = "CFileList.tsv"
        val fileList = tempFolder.createFile(fileListName, "test file list")
        val inputStream = slot<InputStream>()

        every { source.getFile(fileListName) } returns NfsBioFile(fileList)
        every { serializer.deserializeFileList(capture(inputStream), TSV) } throws ClassCastException()

        val exception = assertThrows<InvalidFileListException> {
            testInstance.deserializeFileList(fileListName, source)
        }
        assertThat(exception.message).isEqualTo(
            "Problem processing file list 'CFileList.tsv': The provided page tab doesn't match the file list format"
        )
        verify(exactly = 1) { serializer.deserializeFileList(inputStream.captured, TSV) }
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
    }
}
