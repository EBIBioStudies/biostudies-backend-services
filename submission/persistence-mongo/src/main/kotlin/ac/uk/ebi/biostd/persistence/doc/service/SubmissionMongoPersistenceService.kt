package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.RequestFileList
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import com.mongodb.BasicDBObject
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtProcessingStatus.REQUESTED
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXRWX___
import mu.KotlinLogging
import org.bson.types.ObjectId
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream
import kotlin.math.absoluteValue

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
internal class SubmissionMongoPersistenceService(
    private val subDataRepository: SubmissionDocDataRepository,
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val serializationService: ExtSerializationService,
    private val systemService: FileSystemService,
    private val submissionRepository: ExtSubmissionRepository,
    private val fileListPath: Path,
) : SubmissionPersistenceService {
    override fun saveSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int> {
        val version = getNextVersion(rqt.submission.accNo)
        val extSubmission = rqt.submission.copy(version = version, status = REQUESTED)
        return saveRequest(rqt, extSubmission)
    }

    private fun saveRequest(rqt: SubmissionRequest, extSubmission: ExtSubmission): Pair<String, Int> {
        requestRepository.saveRequest(asRequest(rqt, extSubmission))
        return extSubmission.accNo to extSubmission.version
    }

    override fun processSubmissionRequest(saveRequest: SubmissionRequest): ExtSubmission {
        val (submission, fileMode, draftKey) = saveRequest
        val processingSubmission = processFiles(submission, fileMode)
        val savedSubmission = submissionRepository.saveSubmission(processingSubmission, draftKey)

        // TODO resubmission is removing the files path, page tab is fine though
        requestRepository.updateStatus(SubmissionRequestStatus.PROCESSED, submission.accNo, submission.version)
        systemService.unpublishSubmissionFiles(savedSubmission.accNo, savedSubmission.owner, savedSubmission.relPath)

        if (savedSubmission.released) {
            releaseSubmission(savedSubmission.accNo, savedSubmission.owner, savedSubmission.relPath)
        }

        return savedSubmission
    }

    override fun releaseSubmission(accNo: String, owner: String, relPath: String) {
        logger.info { "$accNo $owner Releasing submission $accNo" }

        subDataRepository.release(accNo)
        systemService.releaseSubmissionFiles(accNo, owner, relPath)

        logger.info { "$accNo $owner Finished releasing submission $accNo" }
    }

    private fun processFiles(submission: ExtSubmission, fileMode: FileMode): ExtSubmission {
        val filePersistenceRequest = FilePersistenceRequest(submission, fileMode)
        return systemService.persistSubmissionFiles(filePersistenceRequest)
    }

    private fun getNextVersion(accNo: String): Int {
        val lastVersion = subDataRepository.getCurrentVersion(accNo) ?: 0
        return lastVersion.absoluteValue + 1
    }

    private fun asRequest(rqt: SubmissionRequest, submission: ExtSubmission): DocSubmissionRequest {
        val content = serializationService.serialize(submission, Properties(includeFileListFiles = false))
        val fileLists = getRequestFileList(submission)
        return DocSubmissionRequest(
            id = ObjectId(),
            accNo = submission.accNo,
            version = submission.version,
            fileMode = rqt.fileMode,
            draftKey = rqt.draftKey,
            status = SubmissionRequestStatus.REQUESTED,
            submission = BasicDBObject.parse(content),
            fileList = fileLists
        )
    }

    private fun getRequestFileList(sub: ExtSubmission): List<RequestFileList> {
        val fileLists = sub.allFileList.distinctBy { it.filePath }
        val baseFolder = getBaseFolder(sub)
        return fileLists.map { asRequestFileList(baseFolder, it) }
    }

    private fun getBaseFolder(sub: ExtSubmission): Path {
        val baseFolder = fileListPath.resolve(sub.accNo).resolve(sub.version.toString())
        FileUtils.createEmptyFolder(baseFolder, RWXRWX___)
        return baseFolder
    }

    private fun asRequestFileList(baseFolder: Path, fileList: ExtFileList): RequestFileList {
        val file = Files.createFile(baseFolder.resolve(fileList.fileName))
        file.outputStream().use { serializationService.serialize(fileList.files.asSequence(), it) }
        return RequestFileList(fileName = fileList.fileName, filePath = file.absolutePathString())
    }
}
