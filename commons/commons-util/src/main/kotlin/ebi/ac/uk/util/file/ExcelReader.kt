package ebi.ac.uk.util.file

import com.monitorjbl.xlsx.StreamingReader
import ebi.ac.uk.dsl.Tsv
import ebi.ac.uk.dsl.TsvLine
import org.apache.poi.ss.usermodel.Sheet
import java.io.File

const val BUFFER_SIZE = 4096
const val ROW_CACHE_SIZE = 1000

class ExcelReader {
    fun readContentAsTsv(file: File): String =
        StreamingReader.builder()
            .rowCacheSize(ROW_CACHE_SIZE)
            .bufferSize(BUFFER_SIZE)
            .open(file)
            .use { Tsv(getTsvLines(it.getSheetAt(0))).toString() }

    private fun getTsvLines(sheet: Sheet) =
        sheet.map(
            { TsvLine(it.map { cell -> cell.valueAsString }) },
            { TsvLine() })
}
