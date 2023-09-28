package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.reactive.awaitLast
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
private const val FILES_CHUNK_SIZE = 100

class ExtSubmissionRepository(
    private val subDataRepository: SubmissionDocDataRepository,
    private val fileListDocFileRepository: FileListDocFileRepository,
    private val toExtSubmissionMapper: ToExtSubmissionMapper,
    private val toDocSubmissionMapper: ToDocSubmissionMapper,
) {
    suspend fun saveSubmission(submission: ExtSubmission): ExtSubmission {
        val saved = persistSubmission(submission)
        return toExtSubmissionMapper.toExtSubmission(saved, false)
    }

    suspend fun expirePreviousVersions(accNo: String) {
        subDataRepository.expireVersions(listOf(accNo))
    }

    private suspend fun persistSubmission(submission: ExtSubmission): DocSubmission {
        val accNo = submission.accNo
        val owner = submission.owner

        logger.info { "$accNo $owner Started mapping submission into doc submission" }
        val (docSubmission, files) = toDocSubmissionMapper.convert(submission)
        logger.info { "$accNo $owner Finished mapping submission into doc submission" }

        logger.info { "$accNo $owner Started saving submission in the database" }
        val savedSubmission = subDataRepository.saveSubmission(docSubmission)
        logger.info { "$accNo $owner Finished saving submission in the database" }

        logger.info { "$accNo $owner Started saving ${files.count()} file list files" }
        files.chunked(FILES_CHUNK_SIZE).forEach { fileListDocFileRepository.saveAll(it).awaitLast() }
        logger.info { "$accNo $owner Finished saving ${files.count()} file list files" }

        return savedSubmission
    }
}
