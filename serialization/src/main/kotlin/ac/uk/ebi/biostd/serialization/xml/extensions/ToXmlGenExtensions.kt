package ac.uk.ebi.biostd.serialization.xml.extensions

import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import javax.xml.namespace.QName

fun ToXmlGenerator.writeAttr(name: String, value: String?) {
    value?.let {
        this.setNextIsAttribute(true)
        this.writeObjectField(name, value)
        this.setNextIsAttribute(false)
    }
}

fun <T> ToXmlGenerator.writeObj(name: String, value: T, function: T.() -> Unit) {
    value?.let {
        setNextName(QName(name))
        writeStartObject()
        function(it)
        writeEndObject()
    }
}

fun <T> ToXmlGenerator.writeCollection(values: Collection<T>, function: (T) -> Unit) {
    writeStartArray()
    values.forEach { function(it) }
    writeEndArray()
}

fun ToXmlGenerator.writeField(name: String, value: Any) {
    writeObjectField(name, value)
}


