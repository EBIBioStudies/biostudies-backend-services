package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.ext.convertList
import ac.uk.ebi.biostd.ext.findNode
import ac.uk.ebi.biostd.ext.getNode
import arrow.core.Either
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constans.SectionFields

object SectionsType : TypeReference<MutableList<Either<Section, SectionsTable>>>()
object LinksType : TypeReference<MutableList<Either<Link, LinksTable>>>()
object FileType : TypeReference<MutableList<Either<File, FilesTable>>>()

class SectionJsonDeserializer : StdDeserializer<Section>(Section::class.java) {

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Section {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        return Section(
            accNo = node.findNode<TextNode>(SectionFields.ACC_NO.value)?.textValue(),
            type = node.getNode<TextNode>(SectionFields.TYPE.value).textValue(),
            attributes = mapper.convertList(node.findNode<JsonNode?>(SectionFields.ATTRIBUTES.value)),
            links = mapper.convertList(node.findNode<JsonNode?>(SectionFields.LINKS.value), LinksType),
            files = mapper.convertList(node.findNode<JsonNode?>(SectionFields.FILES.value), FileType),
            sections = mapper.convertList(node.findNode<JsonNode?>(SectionFields.SUBSECTIONS.value), SectionsType))
    }
}
