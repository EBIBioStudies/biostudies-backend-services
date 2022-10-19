package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestIndexedFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ebi.ac.uk.extended.model.ExtFile
import org.bson.types.ObjectId
import java.util.stream.Stream

class SubmissionRequestFilesMongoPersistenceService(
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val requestFilesRepository: SubmissionRequestFilesRepository,
) : SubmissionRequestFilesPersistenceService {
    override fun upsertSubmissionRequestFile(file: SubmissionRequestFile) {
        val current = requestFilesRepository.findByPathAndAccNoAndVersion(file.path, file.accNo, file.version)

        if (current == null) saveSubmissionRequestFile(file)
        else updateSubmissionRequestFile(file.accNo, file.version, file.path, file.file)
    }

    private fun saveSubmissionRequestFile(file: SubmissionRequestFile) {
        val docFile = DocSubmissionRequestFile(
            ObjectId(),
            file.index,
            file.accNo,
            file.version,
            file.path,
            file.file.toDocFile(),
        )

        requestFilesRepository.save(docFile)
    }

    private fun updateSubmissionRequestFile(accNo: String, version: Int, path: String, file: ExtFile) {
        requestRepository.updateSubmissionRequestFile(accNo, version, path, file.toDocFile())
    }

    override fun getSubmissionRequestFile(path: String, accNo: String, version: Int): ExtFile {
        val docFile = requestFilesRepository.getByPathAndAccNoAndVersion(path, accNo, version)
        return docFile.file.toExtFile()
    }

    override fun getSubmissionRequestFiles(
        accNo: String,
        version: Int,
        startingAt: Int,
    ): Stream<SubmissionRequestIndexedFile> {
        return requestFilesRepository
            .findAllByAccNoAndVersionAndIndexGreaterThan(accNo, version, startingAt)
            .map { SubmissionRequestIndexedFile(it.index, it.file.toExtFile()) }
    }
}
