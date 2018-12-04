package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.xml.common.writeXmlField
import ac.uk.ebi.biostd.xml.common.writeXmlObj
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.constans.AttributeDetails

class AttributeDetailsSerializer : XmlStdSerializer<AttributeDetail>(AttributeDetail::class.java) {

    override fun serializeXml(value: AttributeDetail, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(AttributeDetails.VAL_QUALIFIER, value) {
                writeXmlField(AttributeDetails.NAME, value.name)
                writeXmlField(AttributeDetails.VALUE, value.value)
            }
        }
    }
}
