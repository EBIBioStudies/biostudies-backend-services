package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission

internal class SubmissionMongoFilesPersistenceService(
    private val fileListDocFileRepository: FileListDocFileDocDataRepository,
) : SubmissionFilesPersistenceService {
    override fun getReferencedFiles(
        sub: ExtSubmission,
        fileListName: String,
    ): Sequence<ExtFile> {
        return fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(sub.accNo, 0, fileListName)
            .asSequence()
            .map { it.file.toExtFile(sub.released, sub.relPath) }
    }

    override fun findReferencedFile(
        sub: ExtSubmission,
        path: String,
    ): ExtFile? {
        return fileListDocFileRepository
            .findBySubmissionAccNoAndSubmissionVersionAndFilePath(sub.accNo, sub.version, path)
            .firstOrNull()
            ?.file
            ?.toExtFile(sub.released, sub.relPath)
    }
}
