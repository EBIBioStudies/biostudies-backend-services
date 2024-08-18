package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestFilesDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class SubmissionRequestFilesMongoPersistenceService(
    private val extSerializationService: ExtSerializationService,
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val requestFilesRepository: SubmissionRequestFilesDocDataRepository,
) : SubmissionRequestFilesPersistenceService {
    override suspend fun saveSubmissionRequestFile(file: SubmissionRequestFile) {
        requestRepository.upsertSubRqtFile(file)
    }

    override suspend fun getSubmissionRequestFile(
        accNo: String,
        version: Int,
        filePath: String,
    ): SubmissionRequestFile =
        requestFilesRepository
            .getByPathAndAccNoAndVersion(filePath, accNo, version)
            .toSubmissionRequestFile()

    override fun getSubmissionRequestFiles(
        accNo: String,
        version: Int,
        startingAt: Int,
    ): Flow<SubmissionRequestFile> =
        requestFilesRepository
            .findRequestFiles(accNo, version, startingAt)
            .map { it.toSubmissionRequestFile() }

    override fun getSubmissionRequestFiles(
        accNo: String,
        version: Int,
        status: RequestFileStatus,
    ): Flow<SubmissionRequestFile> =
        requestFilesRepository
            .findRequestFiles(accNo, version, status)
            .map { it.toSubmissionRequestFile() }

    private fun DocSubmissionRequestFile.toSubmissionRequestFile(): SubmissionRequestFile {
        val file = extSerializationService.deserializeFile(file.toString())
        return SubmissionRequestFile(accNo, version, index, path, file, status, previousSubFile)
    }
}
