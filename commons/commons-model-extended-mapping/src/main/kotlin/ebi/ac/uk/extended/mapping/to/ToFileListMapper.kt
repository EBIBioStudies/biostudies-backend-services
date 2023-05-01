package ebi.ac.uk.extended.mapping.to

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.use
import ebi.ac.uk.model.FileList
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class ToFileListMapper(
    private val serializationService: SerializationService,
    private val extSerializationService: ExtSerializationService,
    private val filesResolver: FilesResolver,
) {
    fun convert(fileList: ExtFileList): FileList = FileList(fileList.filePath, emptyFile(fileList.fileName))

    fun serialize(fileList: ExtFileList, targetFormat: SubFormat, file: File): File {
        toFile(fileList.file, targetFormat, file)
        return file
    }

    fun serialize(fileListFiles: Sequence<ExtFile>, targetFormat: SubFormat, file: File): File {
        toFile(fileListFiles, targetFormat, file)
        return file
    }

    private fun emptyFile(fileName: String): File {
        val targetFile = filesResolver.createEmptyFile(fileName = fileName)
        targetFile.outputStream().use { serializationService.serializeFileList(emptySequence(), SubFormat.JSON, it) }
        return targetFile
    }

    private fun toFile(source: File, targetFormat: SubFormat, target: File): File {
        use(source.inputStream(), target.outputStream()) { input, output -> copy(input, targetFormat, output) }
        return target
    }

    private fun toFile(source: Sequence<ExtFile>, targetFormat: SubFormat, target: File): File {
        target.outputStream().use { copy(source, targetFormat, it) }
        return target
    }

    private fun copy(source: Sequence<ExtFile>, targetFormat: SubFormat, target: OutputStream) {
        val sourceFiles = source.map { it.toFile() }
        serializationService.serializeFileList(sourceFiles, targetFormat, target)
    }

    private fun copy(input: InputStream, targetFormat: SubFormat, target: OutputStream) {
        val sourceFiles = extSerializationService.deserializeList(input).map { it.toFile() }
        serializationService.serializeFileList(sourceFiles, targetFormat, target)
    }
}
