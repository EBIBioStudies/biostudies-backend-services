package ac.uk.ebi.biostd.serialization.xml.extensions

import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import ebi.ac.uk.util.collections.ifNotEmpty
import javax.xml.namespace.QName

fun ToXmlGenerator.writeXmlAttr(name: Any, value: Any?) {
    value?.let {
        setNextIsAttribute(true)
        writeObjectField(name.toString(), value)
        setNextIsAttribute(false)
    }
}

fun ToXmlGenerator.writeBooleanAttr(name: Any, value: Boolean, ignoreFalse: Boolean = true) {
    if (value || !ignoreFalse)
        writeXmlAttr(name, value)
}

inline fun <T> ToXmlGenerator.writeXmlObj(name: Any, value: T, function: T.() -> Unit) {
    value?.let {
        setNextName(QName(name.toString()))
        writeStartObject()
        function(it)
        writeEndObject()
    }
}

fun ToXmlGenerator.writeXmlField(name: Any, value: Any) = writeObjectField(name.toString(), value)

fun ToXmlGenerator.writeXmlCollection(name: Any, value: List<Any>) = value.ifNotEmpty { writeXmlField(name, value) }


