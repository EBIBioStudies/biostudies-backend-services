package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.canonicalName
import uk.ac.ebi.extended.serialization.service.ExtFilesResolver
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File

class ToExtFileListMapper(
    private val serializationService: ExtSerializationService,
    private val extFilesResolver: ExtFilesResolver,
) {
    fun convert(accNo: String, version: Int, fileList: FileList, fileSource: FilesSource): ExtFileList {
        val fileName = fileList.canonicalName
        val extFiles = fileList.referencedFiles.map { it.toExtFile(fileSource) }
        return ExtFileList(fileName, toExtFiles(accNo, version, fileName, extFiles))
    }

    private fun toExtFiles(accNo: String, version: Int, fileName: String, files: List<ExtFile>): File {
        val file = extFilesResolver.createEmptyFile(accNo, version, fileName)
        file.outputStream().use { serializationService.serialize(files.asSequence(), it) }
        return file
    }
}
