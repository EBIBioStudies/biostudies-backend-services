package ac.uk.ebi.biostd.common.deserialization.stream

import com.fasterxml.jackson.core.JsonParser
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.AttributeFields

abstract class StreamDeserializerBuilder<T> {
    protected val fields: MutableMap<String, String> = mutableMapOf()
    protected val attributes: MutableList<Attribute> = mutableListOf()

    open fun loadField(fieldName: String, parser: JsonParser) {
        when (fieldName) {
            AttributeFields.ATTRIBUTE.value -> attributes.add(parser.readValueAs(Attribute::class.java))
            else -> fields[fieldName] = parser.text.trim()
        }
    }

    abstract fun build(): T
}
