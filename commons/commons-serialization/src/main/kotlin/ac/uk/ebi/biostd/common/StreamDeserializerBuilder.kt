package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.ext.getTrimmedText
import ac.uk.ebi.biostd.ext.mapFromBuilder
import ac.uk.ebi.biostd.xml.deserializer.stream.AttributeStreamDeserializerBuilder
import com.fasterxml.jackson.core.JsonParser
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.AttributeFields

abstract class StreamDeserializerBuilder<T> {
    protected val fields: MutableMap<String, String> = mutableMapOf()
    protected val attributes: MutableList<Attribute> = mutableListOf()

    fun loadField(fieldName: String, parser: JsonParser) {
//        fieldName?.let {
            when(fieldName) {
                AttributeFields.ATTRIBUTE.value -> {
//                attributes.addAll(parser.mapFromBuilder(AttributeStreamDeserializerBuilder()))
                    attributes.add(parser.readValueAs(Attribute::class.java))
                }
                else -> fields[fieldName] = parser.getTrimmedText()
            }
//        }
    }

    abstract fun build(): T
}
