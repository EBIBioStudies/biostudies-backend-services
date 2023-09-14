package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Meta.CursorOption
import org.springframework.data.mongodb.repository.Meta
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.Instant

interface SubmissionRequestRepository : MongoRepository<DocSubmissionRequest, String> {
    fun getByAccNoAndVersionAndStatus(
        accNo: String,
        version: Int,
        status: RequestStatus,
    ): DocSubmissionRequest

    fun existsByAccNoAndStatusIn(accNo: String, status: Set<RequestStatus>): Boolean
    fun getByAccNoAndStatusIn(accNo: String, status: Set<RequestStatus>): DocSubmissionRequest

    fun getByAccNoAndVersion(accNo: String, version: Int): DocSubmissionRequest

    fun findByStatusIn(status: Set<RequestStatus>): List<DocSubmissionRequest>

    fun findByStatusInAndModificationTimeLessThan(
        status: Set<RequestStatus>,
        since: Instant,
    ): List<DocSubmissionRequest>

    fun getById(id: ObjectId): DocSubmissionRequest
    fun findByAccNo(accNo: String): List<DocSubmissionRequest>
}

interface SubmissionRequestFilesRepository : MongoRepository<DocSubmissionRequestFile, ObjectId> {
    @Query("{ 'accNo': ?0, 'version': ?1, 'index': { \$gt: ?2 } }", sort = "{ index: 1 }")
    fun findRequestFiles(accNo: String, version: Int, index: Int, pageable: Pageable): Page<DocSubmissionRequestFile>

    fun getByPathAndAccNoAndVersion(path: String, accNo: String, version: Int): DocSubmissionRequestFile
}

interface FileListDocFileRepository : MongoRepository<FileListDocFile, ObjectId> {
    @Meta(cursorBatchSize = 100, flags = [CursorOption.NO_TIMEOUT])
    fun findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
    ): List<FileListDocFile>

    fun findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
        accNo: String,
        version: Int,
        fileListName: String,
    ): List<FileListDocFile>

    @Query("{ 'submissionAccNo': ?0, 'submissionVersion': ?1, 'file.filePath': ?2}")
    fun findBySubmissionAccNoAndSubmissionVersionAndFilePath(
        accNo: String,
        version: Int,
        filePath: String,
    ): List<FileListDocFile>
}
