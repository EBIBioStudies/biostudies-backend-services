package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import kotlinx.coroutines.flow.Flow

class FileListDocFileDocDataRepository(
    private val fileListDocFileRepository: FileListDocFileRepository,
) : FileListDocFileRepository by fileListDocFileRepository {

    fun findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flow<FileListDocFile> {
        return fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListNameOrderByIndexAsc(
                accNo,
                version,
                fileListName,
            )
    }

    fun findByFileList(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flow<FileListDocFile> {
        return fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionAndFileListNameOrderByIndexAsc(
                accNo,
                version,
                fileListName,
            )
    }
}
