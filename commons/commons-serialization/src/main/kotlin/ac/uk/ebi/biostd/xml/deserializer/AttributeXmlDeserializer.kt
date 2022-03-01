package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.common.NAME
import ac.uk.ebi.biostd.common.NAME_ATTRS
import ac.uk.ebi.biostd.common.REFERENCE
import ac.uk.ebi.biostd.common.VALUE
import ac.uk.ebi.biostd.common.VAL_ATTRS
import ac.uk.ebi.biostd.xml.deserializer.common.BaseXmlDeserializer
import ebi.ac.uk.base.asBoolean
import ebi.ac.uk.base.nullIfBlank
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import org.w3c.dom.Node

class AttributeXmlDeserializer(private val detailDeserializer: DetailsXmlDeserializer) :
    BaseXmlDeserializer<Attribute>() {

    override fun deserialize(node: Node): Attribute {
        return Attribute(
            name = node.getNodeAttribute(NAME),
            value = node.findNodeAttribute(VALUE).nullIfBlank(),
            reference = node.findProperty(REFERENCE)?.asBoolean().orFalse(),
            nameAttrs = detailDeserializer.deserializeList(node.getSubNodes(NAME_ATTRS)).toMutableList(),
            valueAttrs = detailDeserializer.deserializeList(node.getSubNodes(VAL_ATTRS)).toMutableList()
        )
    }
}

class DetailsXmlDeserializer : BaseXmlDeserializer<AttributeDetail>() {

    override fun deserialize(node: Node): AttributeDetail {
        return AttributeDetail(
            name = node.getNodeAttribute(NAME),
            value = node.getNodeAttribute(VALUE)
        )
    }
}
