package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File

private val logger = KotlinLogging.logger {}

class ToExtFileListMapper(
    private val fileListDocFileDocDataRepository: FileListDocFileDocDataRepository,
    private val serializationService: ExtSerializationService,
    private val extFilesResolver: FilesResolver,
) {
    /**
     * Maps a DocFileList to corresponding Ext type. Note that empty list is used if includeFileListFiles is false as
     * files list files are not loaded as part of the submission.
     */
    @Suppress("LongParameterList")
    suspend fun toExtFileList(
        fileList: DocFileList,
        subAccNo: String,
        subVersion: Int,
        released: Boolean,
        subRelPath: String,
        includeFileListFiles: Boolean,
    ): ExtFileList {
        fun fileListFiles(): Flow<ExtFile> {
            return fileListDocFileDocDataRepository
                .findByFileList(subAccNo, subVersion, fileList.fileName)
                .map { it.file.toExtFile(released, subRelPath) }
        }

        val files = if (includeFileListFiles) fileListFiles() else emptyFlow()
        return ExtFileList(
            filePath = fileList.fileName,
            file = writeFile(subAccNo, subVersion, fileList.fileName, files),
            pageTabFiles = fileList.pageTabFiles.map { it.toExtFile(released, subRelPath) }
        )
    }

    private suspend fun writeFile(subAccNo: String, subVersion: Int, fileListName: String, files: Flow<ExtFile>): File {
        logger.info { "accNo:'$subAccNo' version: '$subVersion', serializing file list '$fileListName'" }
        val file = extFilesResolver.createExtEmptyFile(subAccNo, subVersion, fileListName)
        file.outputStream().use { serializationService.serialize(files, it) }
        logger.info { "accNo:'$subAccNo' version: '$subVersion', completed file list '$fileListName' serialization" }
        return file
    }
}
