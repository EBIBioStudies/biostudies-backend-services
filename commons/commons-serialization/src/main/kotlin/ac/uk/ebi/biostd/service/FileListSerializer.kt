package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.exception.InvalidFileListException.Companion.directoryCantBeFileList
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.Companion.fromFile
import ac.uk.ebi.biostd.validation.InvalidChunkSizeException
import ebi.ac.uk.errors.FileNotFoundException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.use
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.fileListName
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import java.io.InputStream
import java.io.OutputStream

internal class FileListSerializer(
    private val serializer: PagetabSerializer,
    private val filesResolver: FilesResolver,
) {
    internal fun deserializeFileList(fileName: String, source: FilesSource): FileList = getFileList(fileName, source)

    internal fun deserializeFileList2(inputStream: InputStream): Sequence<ebi.ac.uk.model.File> =
        getFileList2(inputStream)

    private fun getFileList2(inputStream: InputStream): Sequence<ebi.ac.uk.model.File> {
        return serializer.deserializeFileList(inputStream, JSON)
    }

    internal fun deserializeFileList(submission: Submission, source: FilesSource): Submission {
        submission.allSections()
            .filter { section -> section.fileListName != null }
            .map { section -> section to section.fileListName!! }
            .forEach { (section, fileList) -> section.fileList = getFileList(fileList, source) }
        return submission
    }

    private fun getFileList(name: String, fileSource: FilesSource): FileList {
        val src = getSourceFile(name, fileSource)
        val target = filesResolver.createEmptyFile(fileName = name)
        use(src.inputStream(), target.outputStream()) { input, output -> copy(name, input, fromFile(src), output) }
        return FileList(name, target)
    }

    private fun copy(fileName: String, source: InputStream, format: SubFormat, target: OutputStream) {
        runCatching {
            val sourceFiles = serializer.deserializeFileList(source, format)
            serializer.serializeFileList(sourceFiles, SubFormat.JSON, target)
        }.onFailure { throw InvalidFileListException(fileName, errorMsg(it)) }
    }

    private fun getSourceFile(fileList: String, source: FilesSource): File =
        when (val file = source.getFile(fileList)) {
            null -> throw FileNotFoundException(fileList)
            else -> if (file.isFile) file else throw directoryCantBeFileList(fileList)
        }

    private fun errorMsg(exception: Throwable) = when (exception) {
        is ClassCastException,
        is InvalidChunkSizeException -> "The provided page tab doesn't match the file list format"
        else -> exception.message.orEmpty()
    }
}
