package ac.uk.ebi.biostd.extended.serialization

import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.EXT_TYPE
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.ATTRIBUTES
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.URL
import ac.uk.ebi.biostd.extended.constants.ExtType
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtLink

class ExtLinkSerializer : JsonSerializer<ExtLink>() {
    override fun serialize(link: ExtLink, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField(URL, link.url)
        gen.writeObjectField(ATTRIBUTES, link.attributes)
        gen.writeStringField(EXT_TYPE, ExtType.Link.type)
        gen.writeEndObject()
    }
}
