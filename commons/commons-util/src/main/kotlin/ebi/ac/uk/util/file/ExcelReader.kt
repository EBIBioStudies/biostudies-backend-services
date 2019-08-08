package ebi.ac.uk.util.file

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.lang.StringBuilder

class ExcelReader {
    fun readContentAsTsv(file: File): String {
        val sheet = XSSFWorkbook(file.inputStream()).getSheetAt(0)
        val tsv = StringBuilder()

        (0..sheet.lastRowNum).forEach {
            val row = sheet.getRow(it)
            if (row == null) {
                tsv.append("\n")
            } else {
                val cells = row.iterator()
                if (cells.hasNext()) {
                    while (cells.hasNext()) {
                        tsv.append(cells.next().stringCellValue)
                        if (cells.hasNext()) tsv.append("\t")
                    }
                } else {
                    tsv.append("\n")
                }
            }
            tsv.append("\n")
        }

        return tsv.toString()
    }
}
