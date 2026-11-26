package ac.uk.ebi.biostd.tsv.deserialization.stream

import ac.uk.ebi.biostd.tsv.TAB
import ac.uk.ebi.biostd.validation.INVALID_LINKS_TABLE
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_LINK_URL
import kotlinx.coroutines.flow.Flow
import uk.ac.ebi.biostd.model.Link
import uk.ac.ebi.biostd.model.constants.TableFields.LINKS_TABLE
import uk.ac.ebi.biostd.util.collections.destructure
import java.io.InputStream
import java.io.OutputStream

internal class LinkListTsvStreamDeserializer {
    suspend fun serializeLinkList(
        links: Flow<Link>,
        linkList: OutputStream,
    ) {
        linkList.bufferedWriter().use { it.writeElements(links, LINKS_TABLE.value, Link::url, Link::attributes) }
    }

    fun deserializeLinkList(linkList: InputStream): Flow<Link> =
        linkList.readElements(LINKS_TABLE.value, INVALID_LINKS_TABLE) { index, row, headers ->
            deserializeLinkListRow(index, row.split(TAB), headers)
        }

    private fun deserializeLinkListRow(
        index: Int,
        row: List<String>,
        headers: List<String>,
    ): Link {
        val (url, attributes) = row.destructure()
        require(url.isNotBlank()) { throw InvalidElementException("Error at row ${index + 1}: $REQUIRED_LINK_URL") }

        return Link(url, attributes = buildAttributes(attributes, headers))
    }
}
