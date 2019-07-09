package ac.uk.ebi.biostd.tsv.deserialization

import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.File as PageTabFile
import java.io.File

class FileListTsvStreamDeserializer {
    fun deserialize(file: File): FileList {
        val fileList: MutableList<PageTabFile> = mutableListOf()
        val reader = file.inputStream().bufferedReader()
        val header = reader.readLine()

        reader.useLines { lines -> lines.forEach { fileList.add(buildFile(header, it)) } }

        return FileList(file.name, fileList.toList())
    }

    private fun buildFile(header: String, fileRow: String) =
        TsvDeserializer()
            .deserializeElement("$header\n$fileRow", FilesTable::class.java)
            .elements
            .first()
}
