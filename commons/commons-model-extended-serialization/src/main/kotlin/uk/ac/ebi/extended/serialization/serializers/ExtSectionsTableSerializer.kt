package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtSectionTable
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.SECTIONS
import uk.ac.ebi.extended.serialization.constants.ExtType

class ExtSectionsTableSerializer : JsonSerializer<ExtSectionTable>() {
    override fun serialize(
        sectionsTable: ExtSectionTable,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeObjectField(SECTIONS, sectionsTable.sections)
        gen.writeStringField(EXT_TYPE, ExtType.SectionsTable.type)
        gen.writeEndObject()
    }
}
