package uk.ac.ebi.biostd

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.common.SerializationConfig
import uk.ac.ebi.biostd.integration.SubFormat
import uk.ac.ebi.biostd.model.BioFile
import java.io.File
import java.nio.file.Files
import kotlin.io.path.outputStream

fun createFileList(
    vararg files: BioFile,
    format: SubFormat = SubFormat.JSON,
): File =
    runBlocking {
        val file = Files.createTempFile("file-list", "${files.size}-files")
        val serializer = SerializationConfig.serializationService()
        file.outputStream().use { serializer.serializeFileList(files.asFlow(), format, it) }
        file.toFile()
    }
