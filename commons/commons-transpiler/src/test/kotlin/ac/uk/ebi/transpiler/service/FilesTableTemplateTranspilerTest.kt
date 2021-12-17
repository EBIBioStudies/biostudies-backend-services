package ac.uk.ebi.transpiler.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.Tsv
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
import org.assertj.core.api.Assertions.assertThat
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
        mockTemplateProcessor, mockTemplateValidator, mockTemplateMapper, mockSerializationService
    )

    @BeforeEach
    fun setUp() {
        every { mockTemplateProcessor.process(testTemplate, testBaseColumns) } returns testFilesTableTemplate
        every { mockTemplateValidator.validate(testFilesTableTemplate, testFilesPath) }.answers { nothing }
        every { mockTemplateMapper.map(testFilesTableTemplate, testFilesPath, testParentFolder) } returns testFilesTable
        every { mockSerializationService.serializeFileList(testFilesTable, SubFormat.TSV) } returns ""
    }

    @Test
    fun transpile() {
        val result = testInstance.transpile(testTemplate, testBaseColumns, testFilesPath, testParentFolder, Tsv)

        assertThat(result).isEqualTo("")
        verify(exactly = 1) {
            mockTemplateProcessor.process(testTemplate, testBaseColumns)
            mockTemplateValidator.validate(testFilesTableTemplate, testFilesPath)
            mockTemplateMapper.map(testFilesTableTemplate, testFilesPath, testParentFolder)
            mockSerializationService.serializeFileList(testFilesTable, Tsv)
        }
    }
}
