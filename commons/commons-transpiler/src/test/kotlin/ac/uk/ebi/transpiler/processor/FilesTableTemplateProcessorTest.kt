package ac.uk.ebi.transpiler.processor

import ac.uk.ebi.transpiler.common.FilesTableTemplateRow
import ac.uk.ebi.transpiler.exception.INVALID_COLUMN_ERROR_MSG
import ac.uk.ebi.transpiler.exception.InvalidColumnException
import ac.uk.ebi.transpiler.factory.testTemplate
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FilesTableTemplateProcessorTest {
    private val testInstance = FilesTableTemplateProcessor()
    private val baseColumns = listOf("Plate", "Replicate", "Well")

    @Test
    fun process() {
        val template = testInstance.process(testTemplate().toString(), baseColumns)

        assertThat(template.header).containsExactly("Plate", "Replicate", "Well", "Gene Identifier", "Gene Symbol")

        assertThat(template.rows).hasSize(2)
        assertRow(template.rows.first(), "Plate1/rep1/A01", "Plate1", "rep1", "A01", "ynl003c", "pet8")
        assertRow(template.rows.second(), "Plate2/rep2/A02", "Plate2", "rep2", "A02", "ybl104c", "sea4")
    }

    @Test
    fun `process empty template`() {
        val template = testInstance.process("", baseColumns)

        assertThat(template.header).isEmpty()
        assertThat(template.rows).isEmpty()
    }

    @Test
    fun `invalid header`() {
        val exception = assertThrows<InvalidColumnException> {
            testInstance.process(testTemplate().toString(), listOf("Replicate"))
        }

        assertThat(exception).hasMessage(String.format(INVALID_COLUMN_ERROR_MSG, "Replicate", "Plate"))
    }

    private fun assertRow(record: FilesTableTemplateRow, path: String, vararg attributes: String) {
        assertThat(record.path).isEqualTo(path)
        assertThat(record.attributes).containsExactly(*attributes)
    }
}
