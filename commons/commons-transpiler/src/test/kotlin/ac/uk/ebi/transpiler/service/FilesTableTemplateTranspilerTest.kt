package ac.uk.ebi.transpiler.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.Tsv
import ac.uk.ebi.transpiler.common.FilesTableTemplate
import ac.uk.ebi.transpiler.factory.testTemplate
import ac.uk.ebi.transpiler.mapper.FilesTableTemplateMapper
import ac.uk.ebi.transpiler.processor.FilesTableTemplateProcessor
import ac.uk.ebi.transpiler.validator.FilesTableTemplateValidator
import ebi.ac.uk.model.FilesTable
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FilesTableTemplateTranspilerTest(
    @MockK private val templateProcessor: FilesTableTemplateProcessor,
    @MockK private val templateValidator: FilesTableTemplateValidator,
    @MockK private val templateMapper: FilesTableTemplateMapper,
    @MockK private val serializationService: SerializationService,
    temporaryFolder: TemporaryFolder,
) {
    private val testFile = temporaryFolder.createFile("fileSerialization.txt")
    private val slotTempFile = slot<File>()
    private val testFilesTable = FilesTable()
    private val testParentFolder = "files"
    private val testFilesPath = "/some/test/files"
    private val testTemplate = testTemplate().toString()
    private val testFilesTableTemplate = FilesTableTemplate()
    private val testBaseColumns = listOf("Plate", "Replicate", "Well")
    private val testInstance = FilesTableTemplateTranspiler(
        templateProcessor, templateValidator, templateMapper, serializationService
    )

    @BeforeEach
    fun setUp() {
        every { templateProcessor.process(testTemplate, testBaseColumns) } returns testFilesTableTemplate
        every { templateValidator.validate(testFilesTableTemplate, testFilesPath) }.answers { nothing }
        every { templateMapper.map(testFilesTableTemplate, testFilesPath, testParentFolder) } returns testFilesTable
        coEvery { serializationService.serializeTable(testFilesTable, TSV, capture(slotTempFile)) } returns testFile
    }

    @Test
    fun transpile() {
        val result = testInstance.transpile(testTemplate, testBaseColumns, testFilesPath, testParentFolder, Tsv)

        assertThat(result).isEqualTo("")
        coVerify(exactly = 1) {
            templateProcessor.process(testTemplate, testBaseColumns)
            templateValidator.validate(testFilesTableTemplate, testFilesPath)
            templateMapper.map(testFilesTableTemplate, testFilesPath, testParentFolder)
            serializationService.serializeTable(testFilesTable, Tsv, slotTempFile.captured)
        }
    }
}
