package ac.uk.ebi.biostd.serialization.xml.common

import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import ebi.ac.uk.base.whenTrue
import ebi.ac.uk.util.collections.ifNotEmpty
import javax.xml.namespace.QName

typealias XmlWriter = ToXmlGenerator

inline fun <T> XmlWriter.writeXmlObj(name: Any, value: T, function: T.() -> Unit) {
    value?.let {
        setNextName(QName(name.toString()))
        writeStartObject()
        function(it)
        writeEndObject()
    }
}

fun XmlWriter.writeXmlAttr(name: Any, value: Any?) {
    value?.let {
        setNextIsAttribute(true)
        writeObjectField(name.toString(), value)
        setNextIsAttribute(false)
    }
}

fun XmlWriter.writeXmlField(name: Any, value: Any) = writeObjectField(name.toString(), value)
fun XmlWriter.writeXmlCollection(name: Any, value: List<Any>) = value.ifNotEmpty { writeXmlField(name, value) }
fun XmlWriter.writeXmlBooleanAttr(name: Any, value: Boolean, ignoreFalse: Boolean = true) = value.or(ignoreFalse.not()).whenTrue { writeXmlAttr(name, value) }

