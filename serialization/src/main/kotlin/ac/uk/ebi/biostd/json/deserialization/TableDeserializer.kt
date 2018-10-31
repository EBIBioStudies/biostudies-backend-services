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

abstract class TableJsonDeserializer<T : Any>(private val itemType: Class<T>) {
    abstract fun createTable(elements: List<T>): Table<T>

    fun deserialize(jp: JsonParser): Table<T> {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        val listType = mapper.typeFactory.constructCollectionType(List::class.java, itemType)
        return createTable(mapper.convertValue(node, listType))
    }
}

class LinksTableJsonDeserializer : StdDeserializer<LinksTable>(Link::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): LinksTable {
        return deserialize(jp) as LinksTable
    }

    companion object : TableJsonDeserializer<Link>(Link::class.java) {
        override fun createTable(elements: List<Link>): Table<Link> = LinksTable(elements)
    }
}

class FilesTableJsonDeserializer : StdDeserializer<FilesTable>(FilesTable::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): FilesTable {
        return deserialize(jp) as FilesTable
    }

    companion object : TableJsonDeserializer<File>(File::class.java) {
        override fun createTable(elements: List<File>): Table<File> = FilesTable(elements)
    }
}

class SectionsTableJsonDeserializer : StdDeserializer<SectionsTable>(SectionsTable::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): SectionsTable {
        return deserialize(jp) as SectionsTable
    }

    companion object : TableJsonDeserializer<Section>(Section::class.java) {
        override fun createTable(elements: List<Section>): Table<Section> = SectionsTable(elements)
    }
}
