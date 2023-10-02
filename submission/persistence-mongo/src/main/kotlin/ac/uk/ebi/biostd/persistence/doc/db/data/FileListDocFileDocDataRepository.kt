package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.PageRequest

class FileListDocFileDocDataRepository(
    private val fileListDocFileRepository: FileListDocFileRepository,
) : FileListDocFileRepository by fileListDocFileRepository {

    fun findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flow<FileListDocFile> {
        fun getPaged(page: Int, size: Int): Flow<FileListDocFile> {
            return fileListDocFileRepository
                .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(
                    accNo,
                    version,
                    fileListName,
                    PageRequest.of(page, size)
                )
        }
        return pageResultAsFlow(function = { page, size -> getPaged(page, size) })
    }

    fun findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flow<FileListDocFile> {
        fun getPaged(page: Int, size: Int): Flow<FileListDocFile> {
            return fileListDocFileRepository
                .findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
                    accNo,
                    version,
                    fileListName,
                    PageRequest.of(page, size)
                )
        }
        return pageResultAsFlow(function = { page, size -> getPaged(page, size) })
    }
}

private fun <T> pageResultAsFlow(page: Int = 0, limit: Int = 10, function: (Int, Int) -> Flow<T>): Flow<T> {
    return flow {
        var result = function(page, limit).toList()
        while (result.isNotEmpty()) {
            result.forEach { emit(it) }
            result = function(page + 1, limit).toList()
        }
    }
}
