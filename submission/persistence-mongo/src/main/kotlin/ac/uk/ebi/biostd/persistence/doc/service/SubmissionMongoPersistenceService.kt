package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
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
import org.bson.types.ObjectId
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream
import kotlin.math.absoluteValue

@Suppress("LongParameterList")
internal class SubmissionMongoPersistenceService(
    private val subDataRepository: SubmissionDocDataRepository,
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val serializationService: ExtSerializationService,
    private val systemService: FileSystemService,
    private val submissionRepository: ExtSubmissionRepository,
    private val fileListPath: Path,
) : SubmissionRequestService {

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
        requestRepository.updateStatus(SubmissionRequestStatus.PROCESSED, submission.accNo, submission.version)
        return savedSubmission
    }

    /**
     * Process the submission files. TODO: We need to populate previous files to avoid re creating them when using FIRE.
     */
    private fun processFiles(submission: ExtSubmission, fileMode: FileMode): ExtSubmission {
        val filePersistenceRequest = FilePersistenceRequest(submission, fileMode, emptyMap())
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
        Files.deleteIfExists(baseFolder)
        Files.createDirectories(baseFolder)
        return baseFolder
    }

    private fun asRequestFileList(baseFolder: Path, fileList: ExtFileList): RequestFileList {
        val file = Files.createFile(baseFolder.resolve(fileList.fileName))
        file.outputStream().use { serializationService.serialize(fileList.files.asSequence(), it) }
        return RequestFileList(fileName = fileList.fileName, filePath = file.absolutePathString())
    }
}
