package ac.uk.ebi.biostd.extended.serialization

import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.EXT_TYPE
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.SECTIONS
import ac.uk.ebi.biostd.extended.constants.ExtType
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtSectionTable

class ExtSectionsTableSerializer : JsonSerializer<ExtSectionTable>() {
    override fun serialize(sectionsTable: ExtSectionTable, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectField(SECTIONS, sectionsTable.sections)
        gen.writeStringField(EXT_TYPE, ExtType.SectionsTable.type)
        gen.writeEndObject()
    }
}
