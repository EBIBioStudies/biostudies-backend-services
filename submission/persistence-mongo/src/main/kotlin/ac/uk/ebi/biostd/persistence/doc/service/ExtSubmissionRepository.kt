package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.LinkListDocLinkRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocFileListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocLinkListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allLinkList
import kotlinx.coroutines.flow.collect
import mu.KotlinLogging
import org.bson.types.ObjectId
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

private val logger = KotlinLogging.logger {}
private const val PERSISTENCE_CHUNK_SIZE = 100

@Suppress("LongParameterList")
class ExtSubmissionRepository(
    private val subDataRepository: SubmissionDocDataRepository,
    private val fileListDocFileRepository: FileListDocFileRepository,
    private val linkListDocLinkRepository: LinkListDocLinkRepository,
    private val toExtSubmissionMapper: ToExtSubmissionMapper,
    private val toDocSubmissionMapper: ToDocSubmissionMapper,
    private val fileMapper: ToDocFileListMapper,
    private val linkMapper: ToDocLinkListMapper,
    private val serializationService: ExtSerializationService,
) {
    suspend fun saveSubmission(submission: ExtSubmission): ExtSubmission {
        val saved = persistSubmission(submission)
        return toExtSubmissionMapper.toExtSubmission(saved, includeFileListFiles = false, includeLinkListLinks = false)
    }

    suspend fun expirePreviousVersions(accNo: String) {
        subDataRepository.expireVersions(listOf(accNo))
    }

    private suspend fun persistSubmission(submission: ExtSubmission): DocSubmission {
        val accNo = submission.accNo
        val owner = submission.owner

        logger.info { "$accNo $owner Started mapping submission into doc submission" }
        val docSubmission = toDocSubmissionMapper.convert(submission)
        logger.info { "$accNo $owner Finished mapping submission into doc submission" }

        logger.info { "$accNo $owner Started saving file list files" }
        saveFileListFiles(submission, docSubmission.id)
        logger.info { "$accNo $owner Finished saving file list files" }

        logger.info { "$accNo $owner Started saving link list links" }
        saveLinkListLinks(submission, docSubmission.id)
        logger.info { "$accNo $owner Finished saving link list links" }

        logger.info { "$accNo $owner Started saving submission in the database" }
        val savedSubmission = subDataRepository.save(docSubmission)
        logger.info { "$accNo $owner Finished saving submission in the database" }

        return savedSubmission
    }

    private suspend fun saveFileListFiles(
        sub: ExtSubmission,
        subId: ObjectId,
    ) {
        val accNo = sub.accNo
        val version = sub.version
        sub.allFileList.forEach { fileList ->
            fileList.file.inputStream().use { stream ->
                val fileListName = fileList.filePath
                logger.info { "$accNo ${sub.owner} Started saving file list : '$fileListName" }
                serializationService
                    .deserializeFileListAsSequence(stream)
                    .mapIndexed { idx, file -> fileMapper.toDocFile(file, fileListName, idx, subId, accNo, version) }
                    .chunked(PERSISTENCE_CHUNK_SIZE)
                    .forEach { chunk -> fileListDocFileRepository.saveAll(chunk).collect() }
                logger.info { "$accNo ${sub.owner} Finished saving file list : '$fileListName" }
            }
        }
    }

    private suspend fun saveLinkListLinks(
        sub: ExtSubmission,
        subId: ObjectId,
    ) {
        val accNo = sub.accNo
        val version = sub.version
        sub.allLinkList.forEach { linkList ->
            linkList.file.inputStream().use { stream ->
                val linkListName = linkList.filePath
                logger.info { "$accNo ${sub.owner} Started saving link list : '$linkListName" }
                serializationService
                    .deserializeLinkListAsSequence(stream)
                    .mapIndexed { idx, link -> linkMapper.toDocLink(link, linkListName, idx, subId, accNo, version) }
                    .chunked(PERSISTENCE_CHUNK_SIZE)
                    .forEach { chunk -> linkListDocLinkRepository.saveAll(chunk).collect() }
                logger.info { "$accNo ${sub.owner} Finished saving link list : '$linkListName" }
            }
        }
    }
}
