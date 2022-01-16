package ac.uk.ebi.biostd.xml.common

import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import ebi.ac.uk.base.ifTrue
import ebi.ac.uk.util.collections.ifNotEmpty
import javax.xml.namespace.QName

typealias XmlWriter = ToXmlGenerator

inline fun XmlWriter.writeXmlObj(name: Any, function: XmlWriter.() -> Unit) {
    setNextName(QName(name.toString()))
    writeStartObject()
    function()
    writeEndObject()
}

fun XmlWriter.writeXmlAttr(name: Any, value: Any?) {
    setNextIsAttribute(true)
    writeObjectField(name.toString(), value)
    setNextIsAttribute(false)
}

fun XmlWriter.writeXmlField(name: Any, value: Any?) = writeObjectField(name.toString(), value)
fun XmlWriter.writeXmlCollection(name: Any, value: List<Any>) = value.ifNotEmpty { writeXmlField(name, value) }
fun XmlWriter.writeXmlBooleanAttr(name: Any, value: Boolean, ignoreFalse: Boolean = true) =
    value.or(ignoreFalse.not()).ifTrue { writeXmlAttr(name, value) }
