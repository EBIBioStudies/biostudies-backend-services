package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.base.nullIfBlank
import ebi.ac.uk.extended.model.ExtAttribute
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_NAME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_NAME_ATTRS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_REFERENCE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_VALUE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_VAL_ATTRS

class ExtAttributeSerializer : JsonSerializer<ExtAttribute>() {

    override fun serialize(attr: ExtAttribute, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.apply {
            writeStartObject()
            writeStringField(ATTR_NAME, attr.name)
            writeStringField(ATTR_VALUE, attr.value?.nullIfBlank())
            writeBooleanField(ATTR_REFERENCE, attr.reference)
            if (attr.nameAttrs.isNotEmpty()) writeObjectField(ATTR_NAME_ATTRS, attr.nameAttrs)
            if (attr.valueAttrs.isNotEmpty()) writeObjectField(ATTR_VAL_ATTRS, attr.valueAttrs)
            writeEndObject()
        }
    }
}
