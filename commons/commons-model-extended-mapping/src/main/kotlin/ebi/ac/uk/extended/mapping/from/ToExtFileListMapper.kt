package ebi.ac.uk.extended.mapping.from

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.use
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.canonicalName
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class ToExtFileListMapper(
    private val extSerializationService: ExtSerializationService,
    private val serializationService: SerializationService,
    private val filesResolver: FilesResolver,
) {
    fun convert(accNo: String, version: Int, fileList: FileList, fileSource: FilesSource): ExtFileList {
        val name = fileList.canonicalName
        val target = filesResolver.createExtEmptyFile(accNo, version, name)
        return ExtFileList(name, toExtFile(fileList.file, SubFormat.fromFile(fileList.file), target, fileSource))
    }

    private fun toExtFile(source: File, format: SubFormat, target: File, fileSource: FilesSource): File {
        use(source.inputStream(), target.outputStream()) { input, output -> copy(input, format, output, fileSource) }
        return target
    }

    private fun copy(input: InputStream, format: SubFormat, target: OutputStream, fileSource: FilesSource) {
        val sourceFiles = serializationService.deserializeFileList(input, format).map { it.toExtFile(fileSource) }
        extSerializationService.serialize(sourceFiles, target)
    }
}
