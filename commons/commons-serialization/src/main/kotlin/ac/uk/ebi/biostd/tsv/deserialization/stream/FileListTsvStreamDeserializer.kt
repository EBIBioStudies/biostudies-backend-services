package ac.uk.ebi.biostd.tsv.deserialization.stream

import ac.uk.ebi.biostd.tsv.TAB
import ac.uk.ebi.biostd.validation.INVALID_TABLE_ROW
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.constants.TableFields.FILES_TABLE
import ebi.ac.uk.util.collections.destructure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream

internal class FileListTsvStreamDeserializer {
    suspend fun serializeFileList(files: Flow<BioFile>, fileList: OutputStream) {
        fileList.bufferedWriter().use { it.writeFiles(files) }
    }

    private suspend fun BufferedWriter.writeFiles(
        files: Flow<BioFile>,
    ) {
        files
            .collectIndexed { index, file ->
                if (index == 0) writeHeaders(file)
                writeAttributesValues(file)
            }
    }

    private suspend fun BufferedWriter.writeHeaders(file: BioFile) = withContext(Dispatchers.IO) {
        val attrsNames = file.attributes.map { it.name }
        write("Files".plus(TAB).plus(attrsNames.joinToString(TAB.toString())))
        newLine()
    }

    private suspend fun BufferedWriter.writeAttributesValues(file: BioFile) = withContext(Dispatchers.IO) {
        val attrsValues = file.attributes.map { it.value }
        write(file.path.plus(TAB).plus(attrsValues.joinToString(TAB.toString())))
        newLine()
    }

    fun deserializeFileList(fileList: InputStream): Flow<BioFile> {
        val reader = fileList.bufferedReader()
        val (files, headers) = reader.readLine().split(TAB).destructure()
        if (files != FILES_TABLE.value) throw InvalidElementException("First header value should be 'Files'")
        return reader
            .asFlow()
            .filter { it.isNotBlank() }
            .withIndex()
            .map { (index, row) -> deserializeRow(index + 1, row.split(TAB), headers) }
    }

    private fun BufferedReader.asFlow(): Flow<String> {
        return flow {
            var line = readLineInIoThread()
            while (line != null) {
                emit(line)
                line = readLineInIoThread()
            }
        }
    }

    private suspend fun BufferedReader.readLineInIoThread(): String? {
        return withContext(Dispatchers.IO) { readLine() }
    }

    private fun deserializeRow(index: Int, row: List<String>, headers: List<String>): BioFile {
        val (fileName, attributes) = row.destructure()
        require(fileName.isNotBlank()) {
            throw InvalidElementException("Error at row ${index + 1}: $REQUIRED_FILE_PATH")
        }

        return BioFile(fileName, attributes = buildAttributes(attributes, headers, index))
    }

    private fun buildAttributes(fields: List<String>, headers: List<String>, idx: Int): List<Attribute> {
        require(fields.size == headers.size) {
            throw InvalidElementException("Error at row ${idx + 1}: $INVALID_TABLE_ROW")
        }

        return headers.mapIndexed { headerIndex, name -> Attribute(name, fields[headerIndex]) }
    }
}
