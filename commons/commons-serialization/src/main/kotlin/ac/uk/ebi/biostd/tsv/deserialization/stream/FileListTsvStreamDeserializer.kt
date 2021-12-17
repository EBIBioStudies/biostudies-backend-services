package ac.uk.ebi.biostd.tsv.deserialization.stream

import ac.uk.ebi.biostd.tsv.TAB
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import ac.uk.ebi.biostd.validation.InvalidElementException
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.FileList
import ebi.ac.uk.util.collections.destructure
import java.io.BufferedWriter
import java.io.File
import java.io.InputStream
import java.io.OutputStream

internal class FileListTsvStreamDeserializer {
    fun deserialize(file: File): FileList {
        val reader = file.inputStream().bufferedReader()
        val deserializer = FilesTableTsvStreamDeserializer(reader.readLine().split(TAB))
        val filesList = reader.useLines {
            it.mapIndexed { idx, line -> deserializer.deserializeRow(idx, line.split(TAB)) }.toList()
        }
        return FileList(file.name, filesList)
    }

    fun serializeFileList(files: Sequence<ebi.ac.uk.model.File>, outputStream: OutputStream) {
        val writer = outputStream.bufferedWriter()
        writeHeaderNames(files.first(), writer)
        files.forEach { file -> writeAttributesValues(file, writer) }
        writer.close()
    }

    private fun writeHeaderNames(firstFile: ebi.ac.uk.model.File, writer: BufferedWriter) {
        val attrsNames = firstFile.attributes.map { it.name }
        writer.write("Files".plus(TAB).plus(attrsNames.joinToString(TAB.toString())))
        writer.newLine()
    }

    private fun writeAttributesValues(file: ebi.ac.uk.model.File, writer: BufferedWriter) {
        val attrsValues = file.attributes.map { it.value }
        writer.write(file.path.plus(TAB).plus(attrsValues.joinToString(TAB.toString())))
        writer.newLine()
    }

    fun deserializeFileList(inputStream: InputStream): Sequence<ebi.ac.uk.model.File> {
        val reader = inputStream.bufferedReader()
        val (files, headers) = reader.readLine()?.split(TAB)?.destructure() ?: return sequenceOf()
        if (files != "Files") throw InvalidElementException("first header value should be 'Files'")

        return reader.lineSequence().mapIndexed { i, row -> deserializeRow(i + 1, row.split(TAB), headers) }
    }

    private fun deserializeRow(index: Int, row: List<String>, headers: List<String>): ebi.ac.uk.model.File {
        val (fileName, attributes) = row.destructure()
        return ebi.ac.uk.model.File(fileName, attributes = buildAttributes(attributes, headers, index))
    }

    private fun buildAttributes(fields: List<String>, headers: List<String>, index: Int): List<Attribute> {
        if (fields.size != headers.size) {
            throw InvalidElementException(
                "Error at row # $index. The row must have the same attributes as the header"
            )
        }

        return headers.mapIndexed { headerIndex, name -> Attribute(name, fields[headerIndex]) }
    }
}

internal class FilesTableTsvStreamDeserializer(pageTabHeader: List<String>) {
    var header = TsvChunkLine(0, pageTabHeader)

    fun deserializeRow(index: Int, row: List<String>): ebi.ac.uk.model.File {
        val fields = TsvChunkLine(index, row)
        return ebi.ac.uk.model.File(fields.name, attributes = buildAttributes(fields))
    }

    private fun buildAttributes(fields: TsvChunkLine): List<Attribute> {
        val attributeNames = header.rawValues
        val attributeValues = fields.rawValues

        if (attributeValues.size != attributeNames.size) {
            throw InvalidElementException(
                "Error at row # ${fields.index}. The row must have the same attributes as the header"
            )
        }

        return attributeNames.mapIndexed { index, name -> Attribute(name, attributeValues[index]) }
    }
}
