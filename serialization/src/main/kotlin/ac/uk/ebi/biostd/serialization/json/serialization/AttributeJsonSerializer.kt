package ac.uk.ebi.biostd.serialization.json.serialization

import ac.uk.ebi.biostd.serialization.json.common.writeJsonArray
import ac.uk.ebi.biostd.serialization.json.common.writeJsonBoolean
import ac.uk.ebi.biostd.serialization.json.common.writeJsonString
import ac.uk.ebi.biostd.serialization.json.common.writeObj
import ac.uk.ebi.biostd.serialization.json.deserialization.NAME
import ac.uk.ebi.biostd.serialization.json.deserialization.REFERENCE
import ac.uk.ebi.biostd.serialization.json.deserialization.TERMS
import ac.uk.ebi.biostd.serialization.json.deserialization.VALUE
import ac.uk.ebi.biostd.submission.Attribute
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class AttributeJsonSerializer : StdSerializer<Attribute>(Attribute::class.java) {

    override fun isEmpty(provider: SerializerProvider, value: Attribute): Boolean = value.name.isEmpty()

    override fun serialize(attr: Attribute, gen: JsonGenerator, provider: SerializerProvider) {

        gen.writeObj {
            writeJsonString(NAME, attr.name)
            writeJsonString(VALUE, attr.value)
            writeJsonBoolean(REFERENCE, attr.reference)
            writeJsonArray(TERMS, attr.terms, gen::writeObject)
        }
    }
}
