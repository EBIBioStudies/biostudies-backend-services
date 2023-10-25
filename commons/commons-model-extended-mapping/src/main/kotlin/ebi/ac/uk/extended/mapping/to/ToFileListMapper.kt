package ebi.ac.uk.extended.mapping.to

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.use
import ebi.ac.uk.model.FileList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Suppress("TooManyFunctions")
class ToFileListMapper(
    private val serializationService: SerializationService,
    private val extSerializationService: ExtSerializationService,
    private val filesResolver: FilesResolver,
) {
    suspend fun convert(fileList: ExtFileList): FileList = FileList(fileList.filePath, emptyFile(fileList.fileName))

    suspend fun serialize(fileList: ExtFileList, targetFormat: SubFormat, file: File): File {
        toFile(fileList.file, targetFormat, file)
        return file
    }

    fun serialize(fileListFiles: Sequence<ExtFile>, targetFormat: SubFormat, file: File): File {
        toFile(fileListFiles, targetFormat, file)
        return file
    }

    suspend fun serialize(fileListFiles: Flow<ExtFile>, targetFormat: SubFormat, file: File): File {
        toFile(fileListFiles, targetFormat, file)
        return file
    }

    private suspend fun emptyFile(fileName: String): File {
        val targetFile = filesResolver.createEmptyFile(fileName = fileName)
        targetFile.outputStream().use { serializationService.serializeFileList(emptyFlow(), SubFormat.JSON, it) }
        return targetFile
    }

    private suspend fun toFile(source: File, targetFormat: SubFormat, target: File): File {
        use(source.inputStream(), target.outputStream()) { input, output -> copy(input, targetFormat, output) }
        return target
    }

    private fun toFile(source: Sequence<ExtFile>, targetFormat: SubFormat, target: File): File {
        target.outputStream().use { copy(source, targetFormat, it) }
        return target
    }

    private suspend fun toFile(source: Flow<ExtFile>, targetFormat: SubFormat, target: File): File {
        target.outputStream().use { copy(source, targetFormat, it) }
        return target
    }

    private fun copy(source: Sequence<ExtFile>, targetFormat: SubFormat, target: OutputStream) {
        val sourceFiles = source.map { it.toFile() }
        serializationService.serializeFileList(sourceFiles, targetFormat, target)
    }

    private suspend fun copy(source: Flow<ExtFile>, targetFormat: SubFormat, target: OutputStream) {
        val sourceFiles = source.map { it.toFile() }
        serializationService.serializeFileList(sourceFiles, targetFormat, target)
    }

    private suspend fun copy(input: InputStream, targetFormat: SubFormat, target: OutputStream) {
        val sourceFiles = extSerializationService.deserializeListAsFlow(input).map { it.toFile() }
        serializationService.serializeFileList(sourceFiles, targetFormat, target)
    }
}
