package ac.uk.ebi.biostd.json.deserialization.stream

import ac.uk.ebi.biostd.ext.mapFromBuilder
import ac.uk.ebi.biostd.ext.readFromBuilder
import ac.uk.ebi.biostd.xml.deserializer.stream.FileStreamDeserializerBuilder
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonToken
import ebi.ac.uk.model.File as PageTabFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.constants.FileFields
import java.io.File

internal class FileListJsonStreamDeserializer {
    fun deserialize(file: File): FileList {
        val parser = JsonFactory().createParser(file)

        val referencedFiles: List<PageTabFile> = parser.mapFromBuilder(FileStreamDeserializerBuilder())
//        val referencedFiles: MutableList<PageTabFile> = mutableListOf()
//
//        var token = parser.nextToken()
//
//        if (token != JsonToken.START_ARRAY) {
//            throw JsonParseException(parser, "Arrays should start with ${JsonToken.START_ARRAY}")
//        }
//
//        while(token != JsonToken.END_ARRAY) {
//            var path = ""
//            while(parser.nextToken() != JsonToken.END_OBJECT) {
//                parser.nextToken()
//                when(parser.currentName) {
//                    FileFields.PATH.value -> path = parser.text
//                }
//            }
//
//            referencedFiles.add(PageTabFile(path))
//        }
//        val reader = JsonReader(InputStreamReader(file.inputStream(), "UTF-8"))
//        val parser = GsonBuilder().apply {
//            registerTypeAdapter(PageTabFile::class.java, FileJsonStreamDeserializer(JsonSerializer()))
//        }.create()
//
//        reader.beginArray()
//
//        while (reader.hasNext()) {
//            val referencedFile = parser.fromJson<PageTabFile>(reader, PageTabFile::class.java)
//            referencedFiles.add(referencedFile)
//        }
//
//        reader.close()
//
//        return FileList(file.name, referencedFiles.toList())
        return FileList(file.name, referencedFiles)
    }
}
