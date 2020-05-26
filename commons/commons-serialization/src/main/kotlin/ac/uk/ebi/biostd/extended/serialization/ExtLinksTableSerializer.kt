package ac.uk.ebi.biostd.extended.serialization

import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.EXT_TYPE
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.LINKS
import ac.uk.ebi.biostd.extended.constants.ExtType
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtLinkTable

class ExtLinksTableSerializer : JsonSerializer<ExtLinkTable>() {
    override fun serialize(linksTable: ExtLinkTable, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectField(LINKS, linksTable.links)
        gen.writeStringField(EXT_TYPE, ExtType.LinksTable.type)
        gen.writeEndObject()
    }
}
