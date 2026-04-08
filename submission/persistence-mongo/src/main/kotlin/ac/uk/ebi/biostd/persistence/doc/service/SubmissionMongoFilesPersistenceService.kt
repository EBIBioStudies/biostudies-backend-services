package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

internal class SubmissionMongoFilesPersistenceService(
    private val fileListDocFileRepository: FileListDocFileDocDataRepository,
) : SubmissionFilesPersistenceService {
    override fun getReferencedFiles(
        sub: ExtSubmission,
        fileListName: String,
    ): Flow<ExtFile> =
        fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(sub.accNo, sub.version, fileListName)
            .map { it.file.toExtFile(sub.released, sub.relPath) }

    override suspend fun getReferencedFiles(
        sub: ExtSubmission,
        fileListName: String,
        pageable: Pageable,
    ): Page<ExtFile> =
        fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(sub.accNo, sub.version, fileListName, pageable)
            .map { it.file.toExtFile(sub.released, sub.relPath) }

    override suspend fun findReferencedFile(
        sub: ExtSubmission,
        path: String,
    ): ExtFile? =
        fileListDocFileRepository
            .findBySubmissionAccNoAndSubmissionVersionAndFilePath(sub.accNo, sub.version, path)
            .firstOrNull()
            ?.file
            ?.toExtFile(sub.released, sub.relPath)
}
