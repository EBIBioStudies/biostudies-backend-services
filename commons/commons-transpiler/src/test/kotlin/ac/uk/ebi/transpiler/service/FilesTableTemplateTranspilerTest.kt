package ac.uk.ebi.transpiler.service

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.transpiler.common.FilesTableTemplate
import ac.uk.ebi.transpiler.factory.testTemplate
import ac.uk.ebi.transpiler.mapper.FilesTableTemplateMapper
import ac.uk.ebi.transpiler.processor.FilesTableTemplateProcessor
import ac.uk.ebi.transpiler.validator.FilesTableTemplateValidator
import ebi.ac.uk.model.FilesTable
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FilesTableTemplateTranspilerTest(
    @MockK private val mockTemplateProcessor: FilesTableTemplateProcessor,
    @MockK private val mockTemplateValidator: FilesTableTemplateValidator,
    @MockK private val mockTemplateMapper: FilesTableTemplateMapper,
    @MockK private val mockSerializationService: SerializationService
) {
    private val testFilesTable = FilesTable()
    private val testParentFolder = "files"
    private val testFilesPath = "/some/test/files"
    private val testTemplate = testTemplate().toString()
    private val testFilesTableTemplate = FilesTableTemplate()
    private val testBaseColumns = listOf("Plate", "Replicate", "Well")
    private val testInstance = FilesTableTemplateTranspiler(
        mockTemplateProcessor, mockTemplateValidator, mockTemplateMapper, mockSerializationService)

    @BeforeEach
    fun setUp() {
        every { mockSerializationService.serializeElement(testFilesTable, SubFormat.TSV) } returns ""
        every { mockTemplateValidator.validate(testFilesTableTemplate, testFilesPath) }.answers { nothing }
        every { mockTemplateProcessor.process(testTemplate, testBaseColumns) } returns testFilesTableTemplate
        every { mockTemplateMapper.map(testFilesTableTemplate, testFilesPath, testParentFolder) } returns testFilesTable
    }

    @Test
    fun transpile() {
        testInstance.transpile(testTemplate, testBaseColumns, testFilesPath, testParentFolder, SubFormat.TSV)
        verify { mockTemplateProcessor.process(testTemplate, testBaseColumns) }
        verify { mockTemplateMapper.map(testFilesTableTemplate, testFilesPath, testParentFolder) }
        verify { mockSerializationService.serializeElement(testFilesTable, SubFormat.TSV) }
    }
}
