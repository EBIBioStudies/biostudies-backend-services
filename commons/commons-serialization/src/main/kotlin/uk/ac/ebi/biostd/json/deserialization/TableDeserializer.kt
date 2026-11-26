package uk.ac.ebi.biostd.json.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import uk.ac.ebi.biostd.json.exception.EmptyTableException
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.FilesTable
import uk.ac.ebi.biostd.model.Link
import uk.ac.ebi.biostd.model.LinksTable
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.model.SectionsTable
import uk.ac.ebi.biostd.model.Table

internal abstract class TableDeserializer<T : Any, S : Table<T>>(
    private val itemType: Class<T>,
    private val tableCreator: (List<T>) -> S,
) : StdDeserializer<S>(itemType) {
    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): S {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        val listType = mapper.typeFactory.constructCollectionType(List::class.java, itemType)
        val elements: List<T> = mapper.convertValue(node, listType)
        require(elements.isNotEmpty()) { throw EmptyTableException(itemType.simpleName) }

        return tableCreator(elements)
    }
}

internal class LinksTableJsonDeserializer : TableDeserializer<Link, LinksTable>(Link::class.java, ::LinksTable)

internal class FilesTableJsonDeserializer : TableDeserializer<BioFile, FilesTable>(BioFile::class.java, ::FilesTable)

internal class SectionsTableJsonDeserializer : TableDeserializer<Section, SectionsTable>(Section::class.java, ::SectionsTable)
