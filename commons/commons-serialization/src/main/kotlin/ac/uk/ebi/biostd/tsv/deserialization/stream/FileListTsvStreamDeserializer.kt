package ac.uk.ebi.biostd.tsv.deserialization.stream

import ebi.ac.uk.model.FileList
import java.io.File

internal class FileListTsvStreamDeserializer {
    fun deserialize(file: File): FileList {
        val reader = file.inputStream().bufferedReader()
        val deserializer = FilesTableTsvStreamDeserializer(reader.readLine())

        val filesList = reader.useLines {
            return@useLines it.mapIndexed { idx, line -> deserializer.deserializeRow(line, idx) }.toList()
        }

        reader.close()

        return FileList(file.name, filesList)
    }
}
