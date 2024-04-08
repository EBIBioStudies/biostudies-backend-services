package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtAttribute
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_NAME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_NAME_ATTRS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_REFERENCE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_VALUE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_VAL_ATTRS

class ExtAttributeSerializer : JsonSerializer<ExtAttribute>() {
    override fun serialize(
        attr: ExtAttribute,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeStringField(ATTR_NAME, attr.name)
        gen.writeStringField(ATTR_VALUE, attr.value?.let { if (attr.value!!.isBlank()) null else attr.value })
        gen.writeBooleanField(ATTR_REFERENCE, attr.reference)
        gen.writeObjectField(ATTR_NAME_ATTRS, attr.nameAttrs)
        gen.writeObjectField(ATTR_VAL_ATTRS, attr.valueAttrs)
        gen.writeEndObject()
    }
}
