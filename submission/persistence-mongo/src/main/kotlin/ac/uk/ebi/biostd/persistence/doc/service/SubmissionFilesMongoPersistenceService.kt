package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionFilesRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ebi.ac.uk.extended.model.ExtFile
import org.bson.types.ObjectId

class SubmissionFilesMongoPersistenceService(
    private val submissionRepo: SubmissionDocDataRepository,
    private val submissionFilesRepository: SubmissionFilesRepository,
) : SubmissionFilesPersistenceService {
    override fun saveSubmissionFile(file: SubmissionFile) {
        val docFile = DocSubmissionRequestFile(
            id = ObjectId(),
            index = file.index,
            accNo = file.accNo,
            version = file.version,
            path = file.path,
            file = file.file.toDocFile(),
            fileList = file.fileListName
        )
        submissionFilesRepository.save(docFile)
    }

    override fun getSubmissionFile(path: String, accNo: String, version: Int): ExtFile {
        val subRelPath = submissionRepo.getRelPath(accNo)
        val docFile = submissionFilesRepository.getByPathAndAccNoAndVersion(path, accNo, version)
        return docFile.file.toExtFile(subRelPath)
    }

    override fun getSubmissionFiles(accNo: String, version: Int, startingAt: Int): List<Pair<ExtFile, Int>> {
        val subRelPath = submissionRepo.getRelPath(accNo)
        return submissionFilesRepository
            .findAllByAccNoAndVersionAndIndexGreaterThan(accNo, version, startingAt)
            .map { (it.file.toExtFile(subRelPath) to it.index) }
    }

    override fun getFileListFiles(accNo: String, version: Int, fileListName: String): List<ExtFile> {
        val subRelPath = submissionRepo.getRelPath(accNo)
        return submissionFilesRepository
            .findAllByAccNoAndVersionAndFileList(accNo, version, fileListName)
            .map { it.file.toExtFile(subRelPath) }
    }
}
