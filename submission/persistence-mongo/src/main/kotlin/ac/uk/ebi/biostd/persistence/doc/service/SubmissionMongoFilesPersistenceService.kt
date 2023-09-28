package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull

internal class SubmissionMongoFilesPersistenceService(
    private val fileListDocFileRepository: FileListDocFileDocDataRepository,
) : SubmissionFilesPersistenceService {
    override fun getReferencedFiles(
        sub: ExtSubmission,
        fileListName: String,
    ): Flow<ExtFile> {
        return fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(sub.accNo, 0, fileListName)
            .asFlow()
            .map { it.file.toExtFile(sub.released, sub.relPath) }
    }

    override suspend fun findReferencedFile(
        sub: ExtSubmission,
        path: String,
    ): ExtFile? {
        return fileListDocFileRepository
            .findBySubmissionAccNoAndSubmissionVersionAndFilePath(sub.accNo, sub.version, path)
            .awaitFirstOrNull()
            ?.file
            ?.toExtFile(sub.released, sub.relPath)
    }
}
