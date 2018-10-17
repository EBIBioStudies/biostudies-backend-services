package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.AttributeFields
import ac.uk.ebi.biostd.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.xml.common.writeXmlBooleanAttr
import ac.uk.ebi.biostd.xml.common.writeXmlField
import ac.uk.ebi.biostd.xml.common.writeXmlObj
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

class AttributeSerializer : XmlStdSerializer<Attribute>(Attribute::class.java) {

    override fun serializeXml(value: Attribute, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(AttributeFields.ATTRIBUTE, value) {
                writeXmlBooleanAttr(AttributeFields.REFERENCE, value.reference)
                writeXmlField(AttributeFields.NAME, value.name)
                writeXmlField(AttributeFields.VALUE, value.value)
                value.terms.forEach { writeXmlField(AttributeFields.TERM, it) }
            }
        }
    }
}
