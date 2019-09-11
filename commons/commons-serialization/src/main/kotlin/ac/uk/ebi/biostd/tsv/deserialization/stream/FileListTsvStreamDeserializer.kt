package ac.uk.ebi.biostd.tsv.deserialization.stream

import ac.uk.ebi.biostd.tsv.TAB
import ebi.ac.uk.model.FileList
import java.io.File

internal class FileListTsvStreamDeserializer {
    fun deserialize(file: File): FileList {
        val reader = file.inputStream().bufferedReader()
        val deserializer = FilesTableTsvStreamDeserializer(reader.readLine().split(TAB))
        val filesList = reader.useLines {
            it.mapIndexed { idx, line -> deserializer.deserializeRow(idx, line.split(TAB)) }.toList()
        }
        return FileList(file.name, filesList)
    }
}
