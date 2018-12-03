package ac.uk.ebi.biostd.json.serialization

import ac.uk.ebi.biostd.json.common.writeJsonArray
import ac.uk.ebi.biostd.json.common.writeJsonBoolean
import ac.uk.ebi.biostd.json.common.writeJsonString
import ac.uk.ebi.biostd.json.common.writeObj
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constans.AttributeDetails
import ebi.ac.uk.model.constans.AttributeFields

class AttributeJsonSerializer : StdSerializer<Attribute>(Attribute::class.java) {

    override fun isEmpty(provider: SerializerProvider, value: Attribute): Boolean = value.name.isEmpty()

    override fun serialize(attr: Attribute, gen: JsonGenerator, provider: SerializerProvider) {

        gen.writeObj {
            writeJsonString(AttributeFields.NAME, attr.name)
            writeJsonString(AttributeFields.VALUE, attr.value)
            writeJsonBoolean(AttributeFields.REFERENCE, attr.reference)
            writeJsonArray(AttributeDetails.NAME_QUALIFIER, attr.nameAttrs, gen::writeObject)
            writeJsonArray(AttributeDetails.VAL_QUALIFIER, attr.valueAttrs, gen::writeObject)
        }
    }
}
