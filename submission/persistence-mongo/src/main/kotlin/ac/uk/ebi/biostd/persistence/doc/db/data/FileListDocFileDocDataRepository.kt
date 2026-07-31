package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class FileListDocFileDocDataRepository(
    private val fileListDocFileRepository: FileListDocFileRepository,
) : FileListDocFileRepository by fileListDocFileRepository {
    fun findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flow<FileListDocFile> = findUniqueByFileList(accNo, version, fileListName)

    suspend fun findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
        pageable: Pageable,
    ): Page<FileListDocFile> {
        val content =
            findUniqueByFileList(accNo, version, fileListName)
                .drop(Math.toIntExact(pageable.offset))
                .take(pageable.pageSize)
                .toList()
        val total =
            fileListDocFileRepository.countBySubmissionAccNoAndSubmissionVersionAndFileListName(
                accNo,
                version,
                fileListName,
            )
        return PageImpl(content, pageable, total)
    }

    fun findByFileList(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flow<FileListDocFile> = findUniqueByFileList(accNo, version, fileListName)

    private fun findUniqueByFileList(
        accNo: String,
        version: Int,
        fileListName: String,
    ): Flow<FileListDocFile> = findFileListEntries(accNo, version, fileListName).distinct()

    private fun findFileListEntries(
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

    private fun Flow<FileListDocFile>.distinct(): Flow<FileListDocFile> =
        flow {
            val uniqueFiles = mutableSetOf<Pair<String, List<DocAttribute>>>()
            collect { entry ->
                val hash = (entry.file.filePath to entry.file.attributes)
                if (uniqueFiles.add(hash)) emit(entry)
            }
        }
}
