package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtFile
import java.io.File
import java.nio.file.Files
import kotlin.io.path.outputStream

fun createExtFileList(vararg files: ExtFile): File = createExtFileList(files.toList())

fun createExtFileList(files: List<ExtFile>): File {
    val file = Files.createTempFile("file-list", "${files.size}-files")
    file.outputStream().use { ExtSerializationService().serialize(files.asSequence(), it) }
    return file.toFile()
}
