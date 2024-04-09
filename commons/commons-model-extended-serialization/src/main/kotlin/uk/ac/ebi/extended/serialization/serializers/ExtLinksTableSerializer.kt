package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtLinkTable
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.LINKS
import uk.ac.ebi.extended.serialization.constants.ExtType

class ExtLinksTableSerializer : JsonSerializer<ExtLinkTable>() {
    override fun serialize(
        linksTable: ExtLinkTable,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeObjectField(LINKS, linksTable.links)
        gen.writeStringField(EXT_TYPE, ExtType.LinksTable.type)
        gen.writeEndObject()
    }
}
