package ebi.ac.uk.util.file

import ebi.ac.uk.dsl.excel.excel
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ExcelReaderTest(private val temporaryFolder: TemporaryFolder) {
    private val testInstance = ExcelReader()

    @Test
    fun `read as TSV`() {
        val testFile = excel("${temporaryFolder.root.absolutePath}/ExcelSubmission.xlsx") {
            sheet("page tab") {
                row {
                    cell("Submission")
                }
                row {
                    cell("Title")
                    cell("Excel Submission")
                }

                emptyRow()

                row {
                    cell("Study")
                    cell("SECT-001")
                    cell("")
                    cell("")
                }
                row {
                    cell("An Attr")
                    cell("A Value")
                }
                row {
                    cell("Numeric Attr")
                    cell("123")
                }

                emptyRow()

                row {
                    cell("Files")
                    cell("Attr 1")
                    cell("Attr 2")
                    cell("")
                }
                row {
                    cell("file1.txt")
                    cell("")
                    cell("a2")
                }
                row {
                    cell("file2.txt")
                    cell("b1")
                    cell("b2")
                }

                emptyRow()
                emptyRow()
                emptyRow()
                emptyRow()
                emptyRow()
            }
        }

        val expectedTsv = tsv {
            line("Submission")
            line("Title", "Excel Submission")
            line()

            line("Study", "SECT-001")
            line("An Attr", "A Value")
            line("Numeric Attr", "123")
            line()

            line("Files", "Attr 1", "Attr 2")
            line("file1.txt", "", "a2")
            line("file2.txt", "b1", "b2")
        }

        assertThat(testInstance.readContentAsTsv(testFile)).isEqualTo(expectedTsv.toString())
    }
}
