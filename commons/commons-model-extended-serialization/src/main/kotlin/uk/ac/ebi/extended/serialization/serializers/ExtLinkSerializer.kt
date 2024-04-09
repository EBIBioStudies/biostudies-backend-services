package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtLink
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.URL
import uk.ac.ebi.extended.serialization.constants.ExtType

class ExtLinkSerializer : JsonSerializer<ExtLink>() {
    override fun serialize(
        link: ExtLink,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeStringField(URL, link.url)
        gen.writeObjectField(ATTRIBUTES, link.attributes)
        gen.writeStringField(EXT_TYPE, ExtType.Link.type)
        gen.writeEndObject()
    }
}
