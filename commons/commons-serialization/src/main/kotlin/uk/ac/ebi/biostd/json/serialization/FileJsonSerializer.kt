package uk.ac.ebi.biostd.json.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import uk.ac.ebi.biostd.json.common.writeJsonArray
import uk.ac.ebi.biostd.json.common.writeJsonNumber
import uk.ac.ebi.biostd.json.common.writeJsonString
import uk.ac.ebi.biostd.json.common.writeObj
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.constants.FileFields

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
