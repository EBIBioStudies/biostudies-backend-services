package ac.uk.ebi.biostd.xml.desirializer

import ac.uk.ebi.biostd.common.NAME
import ac.uk.ebi.biostd.common.NAME_ATTRIBUTES
import ac.uk.ebi.biostd.common.REFERENCE
import ac.uk.ebi.biostd.common.VALUE
import ac.uk.ebi.biostd.common.VAL_ATTRIBUTES
import ac.uk.ebi.biostd.xml.desirializer.common.BaseXmlDeserializer
import ebi.ac.uk.base.asBoolean
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import org.w3c.dom.Node

class AttributeXmlDeserializer(private val detailDeserializer: DetailsXmlDeserializer)
    : BaseXmlDeserializer<Attribute>() {

    override fun deserialize(node: Node): Attribute {
        return Attribute(
            name = node.getNodeAttribute(NAME),
            value = node.getNodeAttribute(VALUE),
            reference = node.findNodeAttribute(REFERENCE)?.asBoolean().orFalse(),
            nameAttrs = detailDeserializer.deserializeList(node.getSubNodes(NAME_ATTRIBUTES)).toMutableList(),
            valueAttrs = detailDeserializer.deserializeList(node.getSubNodes(VAL_ATTRIBUTES)).toMutableList())
    }
}

class DetailsXmlDeserializer : BaseXmlDeserializer<AttributeDetail>() {

    override fun deserialize(node: Node): AttributeDetail {
        return AttributeDetail(
            name = node.getNodeAttribute(NAME),
            value = node.getNodeAttribute(VALUE))
    }
}