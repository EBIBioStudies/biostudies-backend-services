package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import java.util.stream.Stream

class SubmissionRequestFilesMongoPersistenceService(
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val requestFilesRepository: SubmissionRequestFilesRepository,
) : SubmissionRequestFilesPersistenceService {
    override fun upsertSubmissionRequestFile(file: SubmissionRequestFile) {
        requestRepository.upsertSubmissionRequestFile(file)
    }

    override fun getSubmissionRequestFile(
        accNo: String,
        version: Int,
        subRelPath: String,
        filePath: String,
    ): SubmissionRequestFile {
        return requestFilesRepository
            .getByPathAndAccNoAndVersion(filePath, accNo, version)
            .toSubmissionRequestFile(subRelPath)
    }

    override fun getSubmissionRequestFiles(
        accNo: String,
        version: Int,
        subRelPath: String,
        startingAt: Int,
    ): Stream<SubmissionRequestFile> {
        return requestFilesRepository
            .findAllByAccNoAndVersionAndIndexGreaterThan(accNo, version, startingAt)
            .map { it.toSubmissionRequestFile(subRelPath) }
    }

    private fun DocSubmissionRequestFile.toSubmissionRequestFile(subRelPath: String) =
        SubmissionRequestFile(accNo, version, index, path, file.toExtFile(subRelPath))
}
