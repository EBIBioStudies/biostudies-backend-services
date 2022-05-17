package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.service.PageTabFileReader.getFileListFile
import ac.uk.ebi.biostd.validation.InvalidChunkSizeException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.fileListName
import mu.KotlinLogging
import java.io.InputStream

private val logger = KotlinLogging.logger {}

internal class FileListSerializer(
    private val serializer: PagetabSerializer,
) {
    internal fun deserializeFileList(inputStream: InputStream, format: SubFormat): Sequence<BioFile> {
        return serializer.deserializeFileList(inputStream, format)
    }

    internal fun deserializeFileList(submission: Submission, source: FilesSource): Submission {
        submission.allSections()
            .filter { section -> section.fileListName != null }
            .map { section -> section to section.fileListName!! }
            .forEach { (section, fileList) -> section.fileList = getFileList(fileList, source) }
        return submission
    }

    private fun getFileList(name: String, fileSource: FilesSource): FileList {
        val file = getFileListFile(name, fileSource)
        file.inputStream().use { checkFileList(name, SubFormat.fromFile(file), it) }
        return FileList(name, file)
    }

    private fun checkFileList(name: String, format: SubFormat, stream: InputStream) {
        runCatching {
            serializer.deserializeFileList(stream, format).forEach { logger.debug { "read file ${it.path}" } }
        }.getOrElse {
            throw InvalidFileListException(name, errorMsg(it))
        }
    }

    private fun errorMsg(exception: Throwable) = when (exception) {
        is ClassCastException,
        is InvalidChunkSizeException -> "The provided page tab doesn't match the file list format"
        else -> exception.message.orEmpty()
    }
}
