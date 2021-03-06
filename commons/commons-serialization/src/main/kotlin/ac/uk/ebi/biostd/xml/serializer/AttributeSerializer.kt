package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.xml.common.writeXmlBooleanAttr
import ac.uk.ebi.biostd.xml.common.writeXmlField
import ac.uk.ebi.biostd.xml.common.writeXmlObj
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.AttributeDetails
import ebi.ac.uk.model.constants.AttributeFields

class AttributeSerializer : XmlStdSerializer<Attribute>(Attribute::class.java) {

    override fun serializeXml(value: Attribute, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(AttributeFields.ATTRIBUTE) {
                writeXmlBooleanAttr(AttributeFields.REFERENCE, value.reference)
                writeXmlField(AttributeFields.NAME, value.name)
                writeXmlField(AttributeFields.VALUE, value.value)

                // TODO We need to change this from reflection to a proper AttributeDetail serializer
                value.nameAttrs.forEach { writeXmlField(AttributeDetails.NAME_QUALIFIER, it) }
                value.valueAttrs.forEach { writeXmlField(AttributeDetails.VAL_QUALIFIER, it) }
            }
        }
    }
}
