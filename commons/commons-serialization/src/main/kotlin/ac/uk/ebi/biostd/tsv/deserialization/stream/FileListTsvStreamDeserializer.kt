package ac.uk.ebi.biostd.tsv.deserialization.stream

import ac.uk.ebi.biostd.tsv.TAB
import ac.uk.ebi.biostd.validation.INVALID_FILES_TABLE
import ac.uk.ebi.biostd.validation.INVALID_LINKS_TABLE
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_NAME
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ac.uk.ebi.biostd.validation.REQUIRED_LINK_URL
import ebi.ac.uk.io.ext.asFlow
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.constants.TableFields.FILES_TABLE
import ebi.ac.uk.model.constants.TableFields.LINKS_TABLE
import ebi.ac.uk.util.collections.destructure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream

internal class FileListTsvStreamDeserializer {
    suspend fun serializeFileList(
        files: Flow<BioFile>,
        fileList: OutputStream,
    ) {
        fileList.bufferedWriter().use { it.writeFiles(files) }
    }

    private suspend fun BufferedWriter.writeFiles(files: Flow<BioFile>) {
        files
            .collectIndexed { index, file ->
                if (index == 0) writeHeaders(file)
                writeAttributesValues(file)
            }
    }

    private suspend fun BufferedWriter.writeHeaders(file: BioFile) =
        withContext(Dispatchers.IO) {
            val attrsNames = file.attributes.map { it.name }
            write("Files".plus(TAB).plus(attrsNames.joinToString(TAB.toString())))
            newLine()
        }

    private suspend fun BufferedWriter.writeAttributesValues(file: BioFile) =
        withContext(Dispatchers.IO) {
            val attrsValues = file.attributes.map { it.value }
            write(file.path.plus(TAB).plus(attrsValues.joinToString(TAB.toString())))
            newLine()
        }

    fun deserializeFileList(fileList: InputStream): Flow<BioFile> {
        val reader = fileList.bufferedReader()
        val (files, headers) = reader.readLine().split(TAB).destructure()
        require(files == FILES_TABLE.value) { throw InvalidElementException(INVALID_FILES_TABLE) }
        require(headers.none { it.isBlank() }) { throw InvalidElementException(REQUIRED_ATTR_NAME) }

        return reader
            .asFlow()
            .filter { it.isNotBlank() }
            .withIndex()
            .map { (index, row) -> deserializeFileListRow(index + 1, row.split(TAB), headers) }
    }

    fun deserializeLinkList(linkList: InputStream): Flow<Link> {
        val reader = linkList.bufferedReader()
        val (links, headers) = reader.readLine().split(TAB).destructure()
        require(links == LINKS_TABLE.value) { throw InvalidElementException(INVALID_LINKS_TABLE) }
        require(headers.none { it.isBlank() }) { throw InvalidElementException(REQUIRED_ATTR_NAME) }

        return reader
            .asFlow()
            .filter { it.isNotBlank() }
            .withIndex()
            .map { (index, row) -> deserializeLinkListRow(index + 1, row.split(TAB), headers) }
    }

    private fun deserializeFileListRow(
        index: Int,
        row: List<String>,
        headers: List<String>,
    ): BioFile {
        val (fileName, attributes) = row.destructure()
        require(fileName.isNotBlank()) {
            throw InvalidElementException("Error at row ${index + 1}: $REQUIRED_FILE_PATH")
        }

        return BioFile(fileName, attributes = buildAttributes(attributes, headers))
    }

    private fun deserializeLinkListRow(
        index: Int,
        row: List<String>,
        headers: List<String>,
    ): Link {
        val (url, attributes) = row.destructure()
        require(url.isNotBlank()) {
            throw InvalidElementException("Error at row ${index + 1}: $REQUIRED_LINK_URL")
        }

        return Link(url, attributes = buildAttributes(attributes, headers))
    }

    private fun buildAttributes(
        fields: List<String>,
        headers: List<String>,
    ): List<Attribute> =
        fields
            .take(headers.size)
            .mapIndexed { index, value -> Attribute(headers[index], value) }
}
