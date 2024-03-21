package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ebi.ac.uk.coroutines.every
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
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
                .flowOn(Dispatchers.Default)
        }

        val files = if (includeFileListFiles) fileListFiles() else emptyFlow()
        return ExtFileList(
            filePath = fileList.fileName,
            file = writeFileList(subAccNo, subVersion, fileList.fileName, files),
            pageTabFiles = fileList.pageTabFiles.map { it.toExtFile(released, subRelPath) }
        )
    }

    private suspend fun writeFileList(accNo: String, version: Int, name: String, files: Flow<ExtFile>): File {
        suspend fun asLogeableFlow(files: Flow<ExtFile>): Flow<ExtFile> {
            return files.every(
                items = 500,
                { logger.info { "accNo:'$accNo' version: '$version', serialized file ${it.index}, file list '$name'" } }
            )
        }

        logger.info { "accNo:'$accNo' version: '$version', serializing file list '$name'" }
        val file = extFilesResolver.createExtEmptyFile(accNo, version, name)
        file.outputStream().use { serializationService.serialize(asLogeableFlow(files), it) }
        logger.info { "accNo:'$accNo' version: '$version', completed file list '$name' serialization" }
        return file
    }
}
