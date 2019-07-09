package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.JsonSerializer
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.File as PageTabFile
import java.io.File
import java.io.InputStreamReader
import java.lang.reflect.Type

internal class FileListJsonStreamDeserializer {
    fun deserialize(file: File): FileList {
        val referencedFiles: MutableList<PageTabFile> = mutableListOf()
        val reader = JsonReader(InputStreamReader(file.inputStream(), "UTF-8")!!)
        val parser = GsonBuilder().apply {
            registerTypeAdapter(PageTabFile::class.java, FileJsonStreamDeserializer())
        }.create()

        reader.beginArray()

        while (reader.hasNext()) {
            val referencedFile = parser.fromJson<PageTabFile>(reader, PageTabFile::class.java)
            referencedFiles.add(referencedFile)
        }

        reader.close()

        return FileList(file.name, referencedFiles.toList())
    }
}

internal class FileJsonStreamDeserializer : JsonDeserializer<PageTabFile> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?) =
        JsonSerializer().deserialize<PageTabFile>(json!!.toString())
}
