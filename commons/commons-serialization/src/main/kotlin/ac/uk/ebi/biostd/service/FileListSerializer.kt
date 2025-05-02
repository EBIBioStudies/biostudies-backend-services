package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.InvalidFileListException
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.service.PageTabFileReader.getFileListFile
import ac.uk.ebi.biostd.validation.InvalidChunkSizeException
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinkList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.fileListName
import ebi.ac.uk.model.extensions.linkListName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.io.InputStream

internal class FileListSerializer(
    private val serializer: PagetabSerializer,
) {
    internal fun deserializeFileListAsFlow(
        inputStream: InputStream,
        format: SubFormat,
    ): Flow<BioFile> = serializer.deserializeFileListAsFlow(inputStream, format)

    internal fun deserializeLinkListAsFlow(
        inputStream: InputStream,
        format: SubFormat,
    ): Flow<Link> = serializer.deserializeLinkListAsFlow(inputStream, format)

    internal suspend fun deserializeSubmission(
        submission: Submission,
        source: FileSourcesList,
    ): Submission {
        submission
            .allSections()
            .filter { section -> section.fileListName != null }
            .map { section -> section to section.fileListName!! }
            .forEach { (section, fileList) -> section.fileList = getFileList(fileList, source) }

        submission
            .allSections()
            .filter { section -> section.linkListName != null }
            .map { section -> section to section.linkListName!! }
            .forEach { (section, linkList) -> section.linkList = getLinkList(linkList, source) }

        return submission
    }

    private suspend fun getFileList(
        name: String,
        fileSource: FileSourcesList,
    ): FileList {
        val file = getFileListFile(name, fileSource)
        file.inputStream().use { checkFileList(name, SubFormat.fromFile(file), it) }
        return FileList(name, file)
    }

    private suspend fun getLinkList(
        name: String,
        fileSource: FileSourcesList,
    ): LinkList {
        val file = getFileListFile(name, fileSource)
        file.inputStream().use { checkLinkList(name, SubFormat.fromFile(file), it) }
        return LinkList(name, file)
    }

    private suspend fun checkLinkList(
        name: String,
        format: SubFormat,
        stream: InputStream,
    ) {
        runCatching {
            serializer.deserializeLinkListAsFlow(stream, format).collect()
        }.getOrElse {
            throw InvalidFileListException(name, errorMsg(it))
        }
    }

    private suspend fun checkFileList(
        name: String,
        format: SubFormat,
        stream: InputStream,
    ) {
        runCatching {
            serializer.deserializeFileListAsFlow(stream, format).collect()
        }.getOrElse {
            throw InvalidFileListException(name, errorMsg(it))
        }
    }

    private fun errorMsg(exception: Throwable) =
        when (exception) {
            is ClassCastException, is InvalidChunkSizeException,
            -> "The provided page tab doesn't match the file list format"

            else -> exception.message.orEmpty()
        }
}
