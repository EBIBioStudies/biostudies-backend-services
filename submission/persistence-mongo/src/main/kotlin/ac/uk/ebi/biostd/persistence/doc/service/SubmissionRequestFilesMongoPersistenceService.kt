package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import kotlin.streams.asSequence

class SubmissionRequestFilesMongoPersistenceService(
    private val extSerializationService: ExtSerializationService,
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val requestFilesRepository: SubmissionRequestFilesRepository,
) : SubmissionRequestFilesPersistenceService {
    override fun saveSubmissionRequestFile(file: SubmissionRequestFile) {
        requestRepository.upsertSubmissionRequestFile(file)
    }

    override fun getSubmissionRequestFile(accNo: String, version: Int, filePath: String): SubmissionRequestFile {
        return requestFilesRepository
            .getByPathAndAccNoAndVersion(filePath, accNo, version)
            .toSubmissionRequestFile()
    }

    override fun getSubmissionRequestFiles(
        accNo: String,
        version: Int,
        startingAt: Int,
    ): Sequence<SubmissionRequestFile> {
        return requestFilesRepository
            .findAllByAccNoAndVersionAndIndexGreaterThan(accNo, version, startingAt)
            .asSequence()
            .map { it.toSubmissionRequestFile() }
    }

    private fun DocSubmissionRequestFile.toSubmissionRequestFile(): SubmissionRequestFile {
        val file = extSerializationService.deserializeFile(file.toString())
        return SubmissionRequestFile(accNo, version, index, path, file)
    }
}
