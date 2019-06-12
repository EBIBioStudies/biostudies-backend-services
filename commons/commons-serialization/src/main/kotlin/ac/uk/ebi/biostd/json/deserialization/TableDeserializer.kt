package ac.uk.ebi.biostd.json.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Table

internal abstract class TableDeserializer<T : Any, S : Table<T>>(
    private val itemType: Class<T>,
    private val tableCreator: (List<T>) -> S
) : StdDeserializer<S>(itemType) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): S {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        val listType = mapper.typeFactory.constructCollectionType(List::class.java, itemType)
        return tableCreator(mapper.convertValue(node, listType))
    }
}

internal class LinksTableJsonDeserializer : TableDeserializer<Link, LinksTable>(Link::class.java, ::LinksTable)
internal class FilesTableJsonDeserializer : TableDeserializer<File, FilesTable>(File::class.java, ::FilesTable)
internal class SectionsTableJsonDeserializer :
    TableDeserializer<Section, SectionsTable>(Section::class.java, ::SectionsTable)
