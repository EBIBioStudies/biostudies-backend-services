package ac.uk.ebi.biostd.serialization.xml.serializer

import ac.uk.ebi.biostd.serialization.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.serialization.xml.extensions.writeCollection
import ac.uk.ebi.biostd.serialization.xml.extensions.writeObj
import ac.uk.ebi.biostd.submission.Attribute
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

class AttributeSerializer : XmlStdSerializer<Attribute>(Attribute::class.java) {

    override fun serializeXml(value: Attribute, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeObj(value = value, name = "attribute") {
                writeObjectField("name", value.name)
                writeObjectField("value", value.value)
            }
        }
    }
}

class AttrListSerializer(type: JavaType) : XmlStdSerializer<MutableList<Attribute>>(type) {

    override fun serializeXml(value: MutableList<Attribute>, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeCollection(value) {
                writeObject(it)
            }
        }
    }

}
