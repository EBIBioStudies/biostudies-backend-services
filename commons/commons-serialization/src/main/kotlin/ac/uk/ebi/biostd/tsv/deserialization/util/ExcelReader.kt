package ac.uk.ebi.biostd.tsv.deserialization.util

import com.monitorjbl.xlsx.StreamingReader
import ebi.ac.uk.util.file.asTsvList
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
            .use { getTsvLines(it.getSheetAt(0)) }

    private fun getTsvLines(sheet: Sheet) = sheet.asTsvList().joinToString("\n")
}
