package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ebi.ac.uk.extended.model.ExtFile
import org.bson.types.ObjectId
import java.util.stream.Stream

class SubmissionRequestFilesMongoPersistenceService(
    private val requestFilesRepository: SubmissionRequestFilesRepository,
) : SubmissionRequestFilesPersistenceService {
    override fun saveSubmissionRequestFile(file: SubmissionRequestFile) {
        val docFile = DocSubmissionRequestFile(
            ObjectId(),
            file.index,
            file.accNo,
            file.version,
            file.path,
            file.file.toDocFile(),
            file.fileListName
        )

        requestFilesRepository.save(docFile)
    }

    override fun getSubmissionRequestFile(path: String, accNo: String, version: Int): ExtFile {
        val docFile = requestFilesRepository.getByPathAndAccNoAndVersion(path, accNo, version)
        return docFile.file.toExtFile()
    }

    // TODO change pair to object if the index is actually necessary
    override fun getSubmissionRequestFiles(accNo: String, version: Int, startingAt: Int): Stream<Pair<ExtFile, Int>> {
        return requestFilesRepository
            .findAllByAccNoAndVersionAndIndexGreaterThan(accNo, version, startingAt)
            .map { (it.file.toExtFile() to it.index) }
    }

    // TODO this isn't probably necessary
    override fun getRequestFileListFiles(accNo: String, version: Int, fileListName: String): Stream<ExtFile> {
        return requestFilesRepository
            .findAllByAccNoAndVersionAndFileList(accNo, version, fileListName)
            .map { it.file.toExtFile() }
    }
}
