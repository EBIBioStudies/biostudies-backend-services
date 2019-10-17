package ebi.ac.uk.util.file

import com.monitorjbl.xlsx.StreamingReader
import ebi.ac.uk.dsl.Tsv
import ebi.ac.uk.dsl.TsvLine
import java.io.File

const val BUFFER_SIZE = 4096
const val ROW_CACHE_SIZE = 1000

class ExcelReader {
    fun readContentAsTsv(file: File): String {
        val sheet = StreamingReader.builder()
            .rowCacheSize(ROW_CACHE_SIZE)
            .bufferSize(BUFFER_SIZE)
            .open(file.inputStream())
            .getSheetAt(0)

        val tsvLines = sheet.map(
            { TsvLine(it.map { cell -> cell.valueAsString }) },
            { TsvLine() })

        return Tsv(tsvLines).toString()
    }
}
