package ac.uk.ebi.biostd.xml.desirializer

import ac.uk.ebi.biostd.xml.desirializer.common.BaseXmlDeserializer
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields
import org.w3c.dom.Node

class FileXmlDeserializer(private val attributeXmlDeserializer: AttributeXmlDeserializer)
    : BaseXmlDeserializer<File>() {

    override fun deserialize(node: Node): File {
        return File(
            name = node.getNodeAttribute(FileFields.NAME),
            attributes = attributeXmlDeserializer.deserializeList(node.getNode(FileFields.ATTRIBUTES))
        )
    }
}
