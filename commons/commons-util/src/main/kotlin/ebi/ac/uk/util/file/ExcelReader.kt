package ebi.ac.uk.util.file

import com.monitorjbl.xlsx.StreamingReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

const val BUFFER_SIZE = 4096
const val ROW_CACHE_SIZE = 1000

object ExcelReader {

    fun readContentAsTsv(file: File): File {
        val tempFile = File.createTempFile(file.name, ".tmp")
        val reader = StreamingReader.builder().rowCacheSize(ROW_CACHE_SIZE).bufferSize(BUFFER_SIZE).open(file)
        val writer = BufferedWriter(FileWriter(tempFile))
        reader.use {
            it.getSheetAt(0).asTsvList().forEach {
                line ->
                writer.write("$line\n")
            }
        }
        writer.close()
        return tempFile
    }
}
