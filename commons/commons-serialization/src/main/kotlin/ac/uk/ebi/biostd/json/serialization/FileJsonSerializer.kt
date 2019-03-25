package ac.uk.ebi.biostd.json.serialization

import ac.uk.ebi.biostd.json.common.writeJsonArray
import ac.uk.ebi.biostd.json.common.writeJsonNumber
import ac.uk.ebi.biostd.json.common.writeJsonString
import ac.uk.ebi.biostd.json.common.writeObj
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields

class FileJsonSerializer : StdSerializer<File>(File::class.java) {

    override fun isEmpty(provider: SerializerProvider, value: File): Boolean = value.path.isEmpty()

    override fun serialize(file: File, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeObj {
            writeJsonString(FileFields.PATH, file.path)
            writeJsonNumber(FileFields.SIZE.value, file.size)
            writeJsonArray(FileFields.ATTRIBUTES, file.attributes)
            writeJsonString(FileFields.TYPE, FileFields.FILE.value)
        }
    }
}
