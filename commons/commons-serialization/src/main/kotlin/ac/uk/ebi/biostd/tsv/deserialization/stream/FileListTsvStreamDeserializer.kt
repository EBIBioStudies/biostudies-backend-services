package ac.uk.ebi.biostd.tsv.deserialization.stream

import ac.uk.ebi.biostd.tsv.TAB
import ac.uk.ebi.biostd.validation.INVALID_FILES_TABLE
import ac.uk.ebi.biostd.validation.INVALID_TABLE_ROW
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_NAME
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.constants.TableFields.FILES_TABLE
import ebi.ac.uk.util.collections.destructure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream

internal class FileListTsvStreamDeserializer {
    fun serializeFileList(files: Sequence<BioFile>, fileList: OutputStream) {
        val writer = fileList.bufferedWriter()
        processFirstFile(files.first(), writer)
        files.forEach { file -> writeAttributesValues(file, writer) }
        writer.close()
    }

    suspend fun serializeFileList(files: Flow<BioFile>, fileList: OutputStream) {
        val writer = fileList.bufferedWriter()
        processFirstFile(files.first(), writer)
        files.collect { file -> writeAttributesValues(file, writer) }
        writer.close()
    }

    fun deserializeFileList(fileList: InputStream): Sequence<BioFile> {
        val reader = fileList.bufferedReader()
        val (files, headers) = reader.readLine().split(TAB).destructure()
        require(files == FILES_TABLE.value) { throw InvalidElementException(INVALID_FILES_TABLE) }
        require(headers.none { it.isBlank() }) { throw InvalidElementException(REQUIRED_ATTR_NAME) }

        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .mapIndexed { index, row -> deserializeRow(index + 1, row.split(TAB), headers) }
    }

    private fun processFirstFile(firstFile: BioFile, writer: BufferedWriter) {
        val attrsNames = firstFile.attributes.map { it.name }
        writer.write("Files".plus(TAB).plus(attrsNames.joinToString(TAB.toString())))
        writer.newLine()
        writeAttributesValues(firstFile, writer)
    }

    private fun writeAttributesValues(file: BioFile, writer: BufferedWriter) {
        val attrsValues = file.attributes.map { it.value }
        writer.write(file.path.plus(TAB).plus(attrsValues.joinToString(TAB.toString())))
        writer.newLine()
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
