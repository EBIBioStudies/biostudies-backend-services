package ebi.ac.uk.dsl.excel

import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ExcelDslTest(
    private val tempFolder: TemporaryFolder,
) {
    @Test
    fun createExcel() {
        val excelFile = tempFolder.createFile("excelFile.xlsx")
        excel(excelFile) {
            sheet("sheet") {
                row {
                    cell("row 0 cell 0")
                    cell("row 0 cell 1")
                }
                row {
                    cell("row 1 cell 0")
                    cell("row 1 cell 1")
                }
            }
        }

        val sheet = XSSFWorkbook(excelFile.inputStream()).getSheet("sheet")

        val firstRow = sheet.getRow(0)
        assertThat(firstRow.getCell(0).toString()).isEqualTo("row 0 cell 0")
        assertThat(firstRow.getCell(1).toString()).isEqualTo("row 0 cell 1")

        val secondRow = sheet.getRow(1)
        assertThat(secondRow.getCell(0).toString()).isEqualTo("row 1 cell 0")
        assertThat(secondRow.getCell(1).toString()).isEqualTo("row 1 cell 1")
    }
}
