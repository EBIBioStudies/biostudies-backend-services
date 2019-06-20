package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.deserializer.common.BaseXmlDeserializer
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.constants.LinkFields
import org.w3c.dom.Node

class LinkXmlDeserializer(private val attributeXmlDeserializer: AttributeXmlDeserializer)
    : BaseXmlDeserializer<Link>() {

    override fun deserialize(node: Node): Link {
        return Link(
            url = node.getNodeAttribute(LinkFields.URL),
            attributes = attributeXmlDeserializer.deserializeList(node.findNode(LinkFields.ATTRIBUTES)).toMutableList()
        )
    }

    fun deserializeLinksTable(node: Node) = deserializeTable(node, LinkFields.LINK.value, ::LinksTable)
}
