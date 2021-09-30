package ebi.ac.uk.util.file

import com.monitorjbl.xlsx.StreamingReader
import org.apache.poi.ss.usermodel.Sheet
import java.io.File

const val BUFFER_SIZE = 4096
const val ROW_CACHE_SIZE = 1000

object ExcelReader {
    fun readContentAsTsv(file: File): String =
        StreamingReader.builder()
            .rowCacheSize(ROW_CACHE_SIZE)
            .bufferSize(BUFFER_SIZE)
            .open(file)
            .use { getTsvLines(it.getSheetAt(0)) }

    private fun getTsvLines(sheet: Sheet) = sheet.asTsvList().joinToString("\n")
}
