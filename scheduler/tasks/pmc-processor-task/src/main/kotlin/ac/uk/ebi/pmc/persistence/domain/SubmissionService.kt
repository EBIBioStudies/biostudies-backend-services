package ac.uk.ebi.pmc.persistence.domain

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.pmc.persistence.docs.SubFileDocument
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.LOADED
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.PROCESSED
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.PROCESSING
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.SUBMITTED
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.SUBMITTING
import ac.uk.ebi.pmc.persistence.repository.SubFileDataRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionDataRepository
import ac.uk.ebi.pmc.persistence.utils.PmcUtils
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionId
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import org.bson.types.ObjectId
import java.io.File

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class SubmissionService(
    private val subRepository: SubmissionDataRepository,
    private val fileRepository: SubFileDataRepository,
    private val serializationService: SerializationService,
) {
    suspend fun saveLoadedVersion(
        sub: Submission,
        fileName: String,
        subPositionInFile: Int,
    ) {
        val doc =
            SubmissionDocument(
                accNo = sub.accNo,
                body = serializationService.serializeSubmission(sub, JSON),
                status = LOADED,
                sourceFile = fileName,
                posInFile = subPositionInFile,
                sourceTime = PmcUtils.extractSequence(fileName),
            )
        subRepository.saveNew(doc)
    }

    suspend fun saveProcessedSubmission(
        sub: SubmissionDocument,
        files: List<File>,
    ) {
        val newVersion = sub.copy(files = saveFiles(sub.accNo, files), status = PROCESSED)
        subRepository.update(newVersion)
        logger.info { "Finish processing submission with accNo = '${sub.accNo}' from file ${sub.sourceFile}" }
    }

    suspend fun saveSubmittingSubmission(
        sub: SubmissionDocument,
        version: Int,
    ) {
        subRepository.update(sub.copy(version = version, status = SUBMITTED))
    }

    suspend fun saveSubmittingSubmissions(
        submissions: List<SubmissionDocument>,
        results: List<SubmissionId>,
    ) {
        val submitResult = results.associateBy({ it.accNo }, { it.version })
        val result = submissions.map { sub -> sub.copy(version = submitResult.getValue(sub.accNo), status = SUBMITTED) }
        subRepository.update(result)
    }

    fun findReadyToProcess(sourceFile: String?): Flow<SubmissionDocument> =
        flow {
            while (true) {
                val next =
                    when (sourceFile) {
                        null -> subRepository.findAndUpdate(LOADED, PROCESSING)
                        else -> subRepository.findAndUpdate(LOADED, PROCESSING, sourceFile)
                    }
                emit(next ?: break)
            }
        }

    fun findReadyToSubmit(
        sourceFile: String?,
        limit: Int,
    ): Flow<SubmissionDocument> =
        flow {
            var current = 0
            while (current < limit) {
                val next =
                    when (sourceFile) {
                        null -> subRepository.findAndUpdate(PROCESSED, SUBMITTING)
                        else -> subRepository.findAndUpdate(PROCESSED, SUBMITTING, sourceFile)
                    }
                emit(next ?: break)
                current++
            }
        }

    private suspend fun saveFiles(
        accNo: String,
        files: List<File>,
    ): List<ObjectId> =
        coroutineScope {
            files
                .map { SubFileDocument(name = it.name, path = it.absolutePath, accNo = accNo) }
                .map { async { fileRepository.save(it).id } }
                .awaitAll()
        }

    suspend fun reportError(
        sub: SubmissionDocument,
        process: PmcMode,
    ) {
        subRepository.update(sub.copy(status = getError(process)))
    }

    suspend fun reportErrors(
        submissions: List<SubmissionDocument>,
        mode: PmcMode,
    ) {
        subRepository.update(submissions.map { it.copy(status = getError(mode)) })
    }

    private fun getError(pmcMode: PmcMode) =
        when (pmcMode) {
            PmcMode.LOAD -> SubmissionStatus.ERROR_LOAD
            PmcMode.PROCESS -> SubmissionStatus.ERROR_PROCESS
            PmcMode.SUBMIT, PmcMode.SUBMIT_SINGLE -> SubmissionStatus.ERROR_SUBMIT
        }

    suspend fun findById(submissionId: String): SubmissionDocument = subRepository.getById(ObjectId(submissionId))

    fun getSubFiles(files: List<ObjectId>): Flow<SubFileDocument> = fileRepository.findByIds(files)
}
