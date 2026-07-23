package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

class FileListDocFileDocDataRepository(
    private val fileListDocFileRepository: FileListDocFileRepository,
) : FileListDocFileRepository by fileListDocFileRepository {
    fun findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flow<FileListDocFile> =
        fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionAndFileListNameOrderByIndexAsc(
                accNo,
                version,
                fileListName,
            )

    suspend fun findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
        pageable: Pageable,
    ): Page<FileListDocFile> {
        val index = Math.toIntExact(pageable.offset)
        val queryPageable = PageRequest.of(0, pageable.pageSize)
        val records =
            fileListDocFileRepository
                .findAllBySubmissionAccNoAndSubmissionVersionAndFileListNameAndIndexGreaterThanEqualOrderByIndexAsc(
                    accNo,
                    version,
                    fileListName,
                    index,
                    queryPageable,
                )
        val total =
            fileListDocFileRepository.countBySubmissionAccNoAndSubmissionVersionAndFileListName(
                accNo,
                version,
                fileListName,
            )
        return PageImpl(records.toList(), pageable, total)
    }

    fun findByFileList(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flow<FileListDocFile> =
        fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionAndFileListNameOrderByIndexAsc(
                accNo,
                version,
                fileListName,
            )
}
