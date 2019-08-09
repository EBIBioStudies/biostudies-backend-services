package ebi.ac.uk.util.file

import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class ExcelReaderTest {
    private val testInstance = ExcelReader()
    private val testFile = File(this.javaClass::class.java.getResource("/input/ExcelSubmission.xlsx").toURI())

    @Test
    fun `read as TSV`() {
        val expectedTsv = tsv {
            line("Submission")
            line("Title", "Excel Submission")
            line()

            line("Study", "SECT-001")
            line("An Attr", "A Value")
            line()

            line("File", "SomeFile.txt")
            line("Type", "Test File")
        }

        assertThat(testInstance.readContentAsTsv(testFile)).isEqualTo(expectedTsv.toString())
    }
}
