package ac.uk.ebi.biostd.tsv.deserialization.stream

import ac.uk.ebi.biostd.tsv.TAB
import ac.uk.ebi.biostd.validation.INVALID_FILES_TABLE
import ac.uk.ebi.biostd.validation.INVALID_LINKS_TABLE
import ac.uk.ebi.biostd.validation.INVALID_TABLE_ROW
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_NAME
import ac.uk.ebi.biostd.validation.REQUIRED_LINK_URL
import ebi.ac.uk.io.ext.asFlow
import ebi.ac.uk.model.Attribute
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

internal class LinkListTsvStreamDeserializer {
    suspend fun serializeLinkList(
        links: Flow<Link>,
        linkList: OutputStream,
    ) {
        linkList.bufferedWriter().use { it.writeLinks(links) }
    }

    private suspend fun BufferedWriter.writeLinks(links: Flow<Link>) {
        links
            .collectIndexed { index, link ->
                if (index == 0) writeHeaders(link)
                writeAttributesValues(link)
            }
    }

    private suspend fun BufferedWriter.writeHeaders(link: Link) =
        withContext(Dispatchers.IO) {
            val attrsNames = link.attributes.map { it.name }
            write("Links".plus(TAB).plus(attrsNames.joinToString(TAB.toString())))
            newLine()
        }

    private suspend fun BufferedWriter.writeAttributesValues(link: Link) =
        withContext(Dispatchers.IO) {
            val attrsValues = link.attributes.map { it.value }
            write(link.url.plus(TAB).plus(attrsValues.joinToString(TAB.toString())))
            newLine()
        }

    fun deserializeFileList(linkList: InputStream): Flow<Link> {
        val reader = linkList.bufferedReader()
        val (links, headers) = reader.readLine().split(TAB).destructure()
        require(links == FILES_TABLE.value) { throw InvalidElementException(INVALID_FILES_TABLE) }
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
    ): Link {
        val (linkName, attributes) = row.destructure()
        require(linkName.isNotBlank()) {
            throw InvalidElementException("Error at row ${index + 1}: $REQUIRED_LINK_URL")
        }

        return Link(linkName, attributes = buildAttributes(attributes, headers, index))
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

        return Link(url, attributes = buildAttributes(attributes, headers, index))
    }

    private fun buildAttributes(
        fields: List<String>,
        headers: List<String>,
        idx: Int,
    ): List<Attribute> {
        require(fields.size == headers.size) {
            throw InvalidElementException("Error at row ${idx + 1}: $INVALID_TABLE_ROW")
        }

        return headers.mapIndexed { headerIndex, name -> Attribute(name, fields[headerIndex]) }
    }
}
