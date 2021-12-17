package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ac.uk.ebi.biostd.tsv.deserialization.stream.FileListTsvStreamDeserializer
import ac.uk.ebi.biostd.tsv.serialization.TsvToStringSerializer
import ebi.ac.uk.base.splitIgnoringEmpty
import ebi.ac.uk.model.constants.SUB_SEPARATOR
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

internal class TsvSerializer(
    private val tsvSerializer: TsvToStringSerializer = TsvToStringSerializer(),
    private val tsvDeserializer: TsvDeserializer = TsvDeserializer(),
    private val fileListTsvStreamDeserializer: FileListTsvStreamDeserializer = FileListTsvStreamDeserializer()
) {
    fun <T> serialize(element: T) = tsvSerializer.serialize(element)

    fun <T> deserializeElement(pageTab: String, type: Class<out T>) = tsvDeserializer.deserializeElement(pageTab, type)

    fun deserialize(submission: String) = tsvDeserializer.deserialize(submission)

    fun deserializeList(submissions: String) = submissions.splitIgnoringEmpty(SUB_SEPARATOR).map(::deserialize)

    fun serializeFileList(files: List<ebi.ac.uk.model.File>): File {
        val file = Files.createFile(Paths.get("serializedFileList.txt")).toFile()
        file.outputStream().use { fileListTsvStreamDeserializer.serializeFileList(files.asSequence(), it) }
        return file
    }

    fun deserializeFileList(file: File): List<ebi.ac.uk.model.File> =
        file.inputStream().use { fileListTsvStreamDeserializer.deserializeFileList(it).toList() }
}
