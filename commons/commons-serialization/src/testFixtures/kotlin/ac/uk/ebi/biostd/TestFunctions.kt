package ac.uk.ebi.biostd

import ac.uk.ebi.biostd.common.SerializationConfig
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.model.BioFile
import java.io.File
import java.nio.file.Files
import kotlin.io.path.outputStream

fun createFileList(vararg files: BioFile, format: SubFormat = SubFormat.JSON): File {
    val file = Files.createTempFile("file-list", "${files.size}-files")
    val serializer = SerializationConfig.serializationService()
    file.outputStream().use { serializer.serializeFileList(files.asSequence(), format, it) }
    return file.toFile()
}
