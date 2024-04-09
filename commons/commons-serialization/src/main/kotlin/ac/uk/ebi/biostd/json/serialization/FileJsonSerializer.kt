package ac.uk.ebi.biostd.json.serialization

import ac.uk.ebi.biostd.json.common.writeJsonArray
import ac.uk.ebi.biostd.json.common.writeJsonNumber
import ac.uk.ebi.biostd.json.common.writeJsonString
import ac.uk.ebi.biostd.json.common.writeObj
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.constants.FileFields

internal class FileJsonSerializer : StdSerializer<BioFile>(BioFile::class.java) {
    override fun isEmpty(
        provider: SerializerProvider,
        value: BioFile,
    ): Boolean = value.path.isEmpty()

    override fun serialize(
        file: BioFile,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeObj {
            writeJsonString(FileFields.PATH, file.path)
            writeJsonNumber(FileFields.SIZE.value, file.size)
            writeJsonArray(FileFields.ATTRIBUTES, file.attributes)
            writeJsonString(FileFields.TYPE, file.type)
        }
    }
}
