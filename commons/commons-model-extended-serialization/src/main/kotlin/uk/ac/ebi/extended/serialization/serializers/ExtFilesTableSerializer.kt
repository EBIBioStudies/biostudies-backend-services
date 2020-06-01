package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtFileTable
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES
import uk.ac.ebi.extended.serialization.constants.ExtType

class ExtFilesTableSerializer : JsonSerializer<ExtFileTable>() {
    override fun serialize(filesTable: ExtFileTable, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectField(FILES, filesTable.files)
        gen.writeStringField(EXT_TYPE, ExtType.FilesTable.type)
        gen.writeEndObject()
    }
}
