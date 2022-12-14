package ebi.ac.uk.util.file

import com.monitorjbl.xlsx.StreamingReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStream

const val BUFFER_SIZE = 4096
const val ROW_CACHE_SIZE = 1000

object ExcelReader {

    fun asTsv(file: File): File = file.inputStream().use { asTsv(it, file.name) }

    private fun asTsv(inputStream: InputStream, name: String): File {
        val tempFile = File.createTempFile(name, ".tsv")
        val reader = StreamingReader.builder().rowCacheSize(ROW_CACHE_SIZE).bufferSize(BUFFER_SIZE).open(inputStream)
        val writer = BufferedWriter(FileWriter(tempFile))
        reader.use { it.getSheetAt(0).asTsvSequence().forEach { line -> writer.write("$line\n") } }
        writer.close()
        return tempFile
    }
}
