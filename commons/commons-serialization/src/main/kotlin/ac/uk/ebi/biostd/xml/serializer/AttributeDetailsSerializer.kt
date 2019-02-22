package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.xml.common.XmlStdSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.constants.AttributeDetails

class AttributeDetailsSerializer : XmlStdSerializer<AttributeDetail>(AttributeDetail::class.java) {

    override fun serializeXml(value: AttributeDetail, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeStringField(AttributeDetails.NAME.toString(), value.name)
            writeStringField(AttributeDetails.VALUE.toString(), value.value)
        }
    }
}
