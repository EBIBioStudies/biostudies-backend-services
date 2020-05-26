package ac.uk.ebi.biostd.extended.serialization

import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.EXT_TYPE
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.FILES
import ac.uk.ebi.biostd.extended.constants.ExtType
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtFileTable

class ExtFilesTableSerializer : JsonSerializer<ExtFileTable>() {
    override fun serialize(filesTable: ExtFileTable, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectField(FILES, filesTable.files)
        gen.writeStringField(EXT_TYPE, ExtType.FilesTable.type)
        gen.writeEndObject()
    }
}
