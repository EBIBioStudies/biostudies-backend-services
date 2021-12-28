package ac.uk.ebi.biostd.json.deserialization

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
import ebi.ac.uk.model.constants.SectionFields
import uk.ac.ebi.serialization.extensions.convertList
import uk.ac.ebi.serialization.extensions.findNode

internal object SectionsType : TypeReference<MutableList<Either<Section, SectionsTable>>>()
internal object LinksType : TypeReference<MutableList<Either<Link, LinksTable>>>()
internal object FileType : TypeReference<MutableList<Either<File, FilesTable>>>()

internal class SectionJsonDeserializer : StdDeserializer<Section>(Section::class.java) {

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Section {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        return Section(
            accNo = node.findNode<TextNode>(SectionFields.ACC_NO.value)?.textValue(),
            type = node.findNode<TextNode>(SectionFields.TYPE.value)?.textValue().orEmpty(),
            attributes = mapper.convertList(node.findNode(SectionFields.ATTRIBUTES.value)),
            links = node.findNode<JsonNode>(SectionFields.LINKS.value)?.let { mapper.convertValue(it, LinksType) }
                .orEmpty().toMutableList(),
            files = mapper.convertList(node.findNode(SectionFields.FILES.value), FileType),
            sections = mapper.convertList(node.findNode(SectionFields.SUBSECTIONS.value), SectionsType)
        )
    }
}
