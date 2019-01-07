package ac.uk.ebi.biostd.xml.desirializer

import ac.uk.ebi.biostd.xml.desirializer.common.BaseXmlDeserializer
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.constants.LinkFields
import org.w3c.dom.Node

class LinkXmlDeserializer(private val attributeXmlDeserializer: AttributeXmlDeserializer)
    : BaseXmlDeserializer<Link>() {

    override fun deserialize(node: Node): Link {
        return Link(
            url = node.getNodeAttribute(LinkFields.URL),
            attributes = attributeXmlDeserializer.deserializeList(node.getNode(LinkFields.ATTRIBUTES))
        )
    }
}
