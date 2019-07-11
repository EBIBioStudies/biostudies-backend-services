package ac.uk.ebi.biostd.json.deserialization.stream

import ac.uk.ebi.biostd.common.deserialization.stream.AttributeStreamDeserializerBuilder
import ac.uk.ebi.biostd.ext.mapFromBuilder
import ac.uk.ebi.biostd.ext.startArray
import com.fasterxml.jackson.core.JsonFactory
import ebi.ac.uk.model.FileList
import java.io.File

internal class FileListJsonStreamDeserializer {
    fun deserialize(file: File): FileList {
        val parser = JsonFactory().createParser(file)
        val fileBuilder = FileJsonStreamDeserializerBuilder(AttributeStreamDeserializerBuilder())

        parser.startArray()

        val referencedFiles = parser.mapFromBuilder(fileBuilder)

        parser.close()

        return FileList(file.name, referencedFiles)
    }
}
