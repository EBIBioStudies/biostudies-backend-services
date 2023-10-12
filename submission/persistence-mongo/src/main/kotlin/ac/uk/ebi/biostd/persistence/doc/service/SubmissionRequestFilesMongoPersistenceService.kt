package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class SubmissionRequestFilesMongoPersistenceService(
    private val extSerializationService: ExtSerializationService,
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val requestFilesRepository: SubmissionRequestFilesRepository,
) : SubmissionRequestFilesPersistenceService {
    override suspend fun saveSubmissionRequestFile(file: SubmissionRequestFile) {
        requestRepository.upsertSubmissionRequestFile(file)
    }

    override suspend fun getSubmissionRequestFile(
        accNo: String,
        version: Int,
        filePath: String,
    ): SubmissionRequestFile {
        return requestFilesRepository
            .getByPathAndAccNoAndVersion(filePath, accNo, version)
            .toSubmissionRequestFile()
    }

    override fun getSubmissionRequestFiles(
        accNo: String,
        version: Int,
        startingAt: Int,
    ): Flow<SubmissionRequestFile> {
        return requestFilesRepository
            .findRequestFiles(accNo, version, startingAt)
            .map { it.toSubmissionRequestFile() }
    }

    private fun DocSubmissionRequestFile.toSubmissionRequestFile(): SubmissionRequestFile {
        val file = extSerializationService.deserializeFile(file.toString())
        return SubmissionRequestFile(accNo, version, index, path, file)
    }
}
