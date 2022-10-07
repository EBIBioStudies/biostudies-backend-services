package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionFilesRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionFile
import ebi.ac.uk.extended.model.ExtFile
import org.bson.types.ObjectId

class SubmissionFilesMongoPersistenceService(
    private val fileListDocFileRepository: FileListDocFileRepository,
    private val submissionFilesRepository: SubmissionFilesRepository,
) : SubmissionFilesPersistenceService {
    override fun saveSubmissionFile(file: SubmissionFile) {
        val docFile = DocSubmissionFile(
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

    override fun getSubmissionFile(path: String, accNo: String, version: Int): ExtFile {
        val docFile = submissionFilesRepository.getByPathAndAccNoAndVersion(path, accNo, version)
        return docFile.file.toExtFile()
    }

    override fun getSubmissionFiles(accNo: String, version: Int, startingAt: Int): List<Pair<ExtFile, Int>> {
        return submissionFilesRepository
            .findAllByAccNoAndVersionAndIndexGreaterThan(accNo, version, startingAt)
            .map { (it.file.toExtFile() to it.index) }
    }

    override fun getFileListFiles(accNo: String, version: Int, fileListName: String): List<ExtFile> {
        return submissionFilesRepository
            .findAllByAccNoAndVersionAndFileList(accNo, version, fileListName)
            .map { it.file.toExtFile() }
    }

    override fun getReferencedFiles(accNo: String, fileListName: String): List<ExtFile> {
        return fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(accNo, 0, fileListName)
            .map { it.file.toExtFile() }
    }
}
