package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionFilesRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ebi.ac.uk.extended.model.ExtFile
import org.bson.types.ObjectId

class SubmissionFilesMongoPersistenceService(
    private val submissionRepo: SubmissionDocDataRepository,
    private val fileListDocFileRepository: FileListDocFileRepository,
    private val submissionFilesRepository: SubmissionFilesRepository,
) : SubmissionFilesPersistenceService {
    override fun saveSubmissionFile(file: SubmissionFile) {
        val docFile = DocSubmissionRequestFile(
            ObjectId(),
            file.index,
            file.accNo,
            file.version,
            file.path,
            file.file.toDocFile(),
            file.fileListName
        )

        submissionFilesRepository.save(docFile)
    }

    override fun getReferencedFiles(accNo: String, fileListName: String): List<ExtFile> {
        return when (val subRelPath = submissionRepo.findRelPath(accNo)) {
            null -> emptyList()
            else ->
                fileListDocFileRepository
                    .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(accNo, 0, fileListName)
                    .map { it.file.toExtFile(subRelPath) }
        }
    }
}
