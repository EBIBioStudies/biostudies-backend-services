package ac.uk.ebi.biostd.serialization.xml.common

import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

fun ToXmlGenerator.writeAttr(name: String, value: String) {
    this.setNextIsAttribute(true)
    this.writeObjectField(name, value)
}
