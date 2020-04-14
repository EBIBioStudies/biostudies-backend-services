package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.file.ExcelReader
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class PageTabSerializationServiceTest(private val tempFolder: TemporaryFolder) {
    private val source = mockk<FilesSource>()
    private val excelReader = mockk<ExcelReader>()
    private val testSubmission = mockk<Submission>()
    private val serializer = mockk<PagetabSerializer>()
    private val fileListSerializer = mockk<FileListSerializer>()
    private val testInstance = PageTabSerializationService(excelReader, serializer, fileListSerializer)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `serialize element`() {
        every { serializer.serializeElement(testSubmission, JSON) } returns ""
        testInstance.serializeElement(testSubmission, JSON)
        verify(exactly = 1) { serializer.serializeElement(testSubmission, JSON) }
    }

    @Test
    fun `serialize submission`() {
        every { serializer.serializeSubmission(testSubmission, JSON) } returns ""
        testInstance.serializeSubmission(testSubmission, JSON)
        verify(exactly = 1) { serializer.serializeSubmission(testSubmission, JSON) }
    }

    @Test
    fun `deserialize submission`() {
        every { serializer.deserializeSubmission("test submission", JSON) } returns testSubmission
        testInstance.deserializeSubmission("test submission", JSON)
        verify(exactly = 1) { serializer.deserializeSubmission("test submission", JSON) }
    }

    @Test
    fun `deserialize submission with files`() {
        every { serializer.deserializeSubmission("test submission", JSON) } returns testSubmission
        every { fileListSerializer.deserializeFileList(testSubmission, source) } returns testSubmission

        testInstance.deserializeSubmission("test submission", JSON, source)

        verify(exactly = 1) {
            serializer.deserializeSubmission("test submission", JSON)
            fileListSerializer.deserializeFileList(testSubmission, source)
        }
    }

    @Test
    fun `deserialize JSON submission with file source`() {
        val file = tempFolder.createFile("submission.json", "test submission")

        every { serializer.deserializeSubmission("test submission", JSON) } returns testSubmission
        every { fileListSerializer.deserializeFileList(testSubmission, source) } returns testSubmission

        testInstance.deserializeSubmission(file, source)

        verify(exactly = 1) {
            serializer.deserializeSubmission("test submission", JSON)
            fileListSerializer.deserializeFileList(testSubmission, source)
        }
    }

    @Test
    fun `deserialize TSV submission`() {
        val file = tempFolder.createFile("submission.tsv", "test submission")

        every { serializer.deserializeSubmission("test submission", TSV) } returns testSubmission
        testInstance.deserializeSubmission(file)
        verify(exactly = 1) { serializer.deserializeSubmission("test submission", TSV) }
    }

    @Test
    fun `deserialize XML submission`() {
        val file = tempFolder.createFile("submission.xml", "test submission")

        every { serializer.deserializeSubmission("test submission", XML) } returns testSubmission
        testInstance.deserializeSubmission(file)
        verify(exactly = 1) { serializer.deserializeSubmission("test submission", XML) }
    }

    @Test
    fun `deserialize XLS submission`() {
        val file = tempFolder.createFile("submission.xlsx")

        every { excelReader.readContentAsTsv(file) } returns "test submission"
        every { serializer.deserializeSubmission("test submission", TSV) } returns testSubmission

        testInstance.deserializeSubmission(file)

        verify(exactly = 1) {
            excelReader.readContentAsTsv(file)
            serializer.deserializeSubmission("test submission", TSV)
        }
    }
}
