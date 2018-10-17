package ac.uk.ebi.biostd.xml.common

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

abstract class XmlStdSerializer<T>(type: Class<T>) : StdSerializer<T>(type) {

    override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
        this.serializeXml(value, gen as ToXmlGenerator, provider)
    }

    abstract fun serializeXml(value: T, gen: ToXmlGenerator, provider: SerializerProvider)
}