package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File

class ToExtFileListMapper(
    private val fileListDocFileRepository: FileListDocFileRepository,
    private val serializationService: ExtSerializationService,
    private val extFilesResolver: FilesResolver,
) {
    /**
     * Maps a DocFileList to corresponding Ext type. Note that empty list is used if includeFileListFiles is false as
     * files list files are not loaded as part of the submission.
     */
    fun toExtFileList(
        fileList: DocFileList,
        subAccNo: String,
        subVersion: Int,
        includeFileListFiles: Boolean,
    ): ExtFileList {
        val files = if (includeFileListFiles) getFiles(subAccNo, subVersion, fileList.fileName) else emptySequence()
        return ExtFileList(
            filePath = fileList.fileName,
            file = writeFile(subAccNo, subVersion, fileList.fileName, files),
            pageTabFiles = fileList.pageTabFiles.map { it.toExtFile() }
        )
    }

    private fun writeFile(subAccNo: String, subVersion: Int, fileListName: String, files: Sequence<ExtFile>): File {
        val file = extFilesResolver.createExtEmptyFile(subAccNo, subVersion, fileListName)
        file.outputStream().use { serializationService.serialize(files, it) }
        return file
    }

    private fun getFiles(subAccNo: String, subVersion: Int, fileListName: String): Sequence<ExtFile> {
        return fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(subAccNo, subVersion, fileListName)
            .map { it.file.toExtFile() }
            .asSequence()
    }
}
