package ac.uk.ebi.biostd

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.model.BioFile
import java.io.File
import java.nio.file.Files
import kotlin.io.path.outputStream

fun createFileList(vararg files: BioFile, format: SubFormat = SubFormat.JSON): File {
    val file = Files.createTempFile("file-list", "${files.size}-files")
    file.outputStream().use { SerializationService().serializeFileList(files.asSequence(), format, it) }
    return file.toFile()
}
