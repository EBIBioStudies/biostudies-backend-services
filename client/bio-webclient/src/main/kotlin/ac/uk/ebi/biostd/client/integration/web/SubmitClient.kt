package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ebi.ac.uk.api.ClientResponse
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.api.dto.UserGroupDto
import ebi.ac.uk.api.security.CheckUserRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.UserProfile
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtPage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtUser
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.model.Collection
import ebi.ac.uk.model.FolderStats
import ebi.ac.uk.model.Group
import ebi.ac.uk.model.MigrateHomeOptions
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionId
import ebi.ac.uk.model.SubmissionStat
import ebi.ac.uk.model.UpdateResult
import ebi.ac.uk.model.WebSubmissionDraft
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.Instant

interface SubmitClient :
    SubmissionOperations,
    FilesOperations,
    GroupFilesOperations,
    SubmitOperations,
    MultipartSubmitOperations,
    MultipartAsyncSubmitOperations,
    GeneralOperations,
    DraftSubmissionOperations,
    ExtSubmissionOperations,
    PostProcessOperations,
    PermissionOperations,
    StatsOperations,
    SubmissionRequestOperations,
    UserOperations

typealias SubmissionResponse = ClientResponse<Submission>

interface FilesOperations {
    suspend fun uploadFiles(
        files: List<File>,
        relativePath: String = EMPTY,
    )

    suspend fun uploadFile(
        file: File,
        relativePath: String = EMPTY,
    )

    suspend fun downloadFile(
        fileName: String,
        relativePath: String = EMPTY,
    ): File

    suspend fun listUserFiles(relativePath: String = EMPTY): List<UserFile>

    suspend fun deleteFile(
        fileName: String,
        relativePath: String = EMPTY,
    )

    suspend fun createFolder(
        folderName: String,
        relativePath: String = EMPTY,
    )
}

interface GroupFilesOperations {
    suspend fun uploadGroupFiles(
        groupName: String,
        files: List<File>,
        relativePath: String = EMPTY,
    )

    suspend fun downloadGroupFile(
        groupName: String,
        fileName: String,
        relativePath: String = EMPTY,
    ): File

    suspend fun listGroupFiles(
        groupName: String,
        relativePath: String = EMPTY,
    ): List<UserFile>

    suspend fun deleteGroupFile(
        groupName: String,
        fileName: String,
        relativePath: String = EMPTY,
    )

    suspend fun createGroupFolder(
        groupName: String,
        folderName: String,
        relativePath: String = EMPTY,
    )
}

interface SubmissionOperations {
    suspend fun deleteSubmission(accNo: String)

    suspend fun deleteSubmissions(submissions: List<String>)

    suspend fun getSubmissions(filter: Map<String, Any> = mapOf()): List<SubmissionDto>

    suspend fun validateFileList(
        fileListPath: String,
        rootPath: String? = null,
        accNo: String? = null,
    )

    suspend fun getSubmission(accNo: String): SubmissionDto?

    suspend fun getSubmissionJson(accNo: String): String?
}

interface SecurityOperations {
    fun getAuthenticatedClient(
        user: String,
        password: String,
        onBehalf: String? = null,
    ): BioWebClient

    fun login(loginRequest: LoginRequest): UserProfile

    fun registerUser(registerRequest: RegisterRequest)

    fun checkUser(checkUserRequest: CheckUserRequest)
}

interface UserOperations {
    suspend fun getExtUser(email: String): ExtUser

    suspend fun getUserHomeStats(email: String): FolderStats

    suspend fun migrateUser(
        email: String,
        options: MigrateHomeOptions,
    ): Unit
}

interface GeneralOperations {
    suspend fun getGroups(): List<Group>

    suspend fun getCollections(): List<Collection>

    suspend fun generateFtpLinks(accNo: String)

    suspend fun createGroup(
        groupName: String,
        groupDescription: String,
    ): UserGroupDto

    suspend fun addUserInGroup(
        groupName: String,
        userName: String,
    )

    suspend fun getProfile(): UserProfile
}

interface StatsOperations {
    fun findByAccNo(accNo: String): Flow<SubmissionStat>

    fun findByType(type: String): Flow<SubmissionStat>

    suspend fun findByTypeAndAccNo(
        type: String,
        accNo: String,
    ): SubmissionStat

    suspend fun register(stat: SubmissionStat): Unit

    suspend fun register(
        type: String,
        statsFile: File,
    ): UpdateResult

    suspend fun incrementStats(
        type: String,
        statsFile: File,
    ): UpdateResult
}

interface DraftSubmissionOperations {
    suspend fun getAllSubmissionDrafts(
        limit: Int = 15,
        offset: Int = 0,
    ): List<WebSubmissionDraft>

    suspend fun getSubmissionDraft(accNo: String): WebSubmissionDraft

    suspend fun deleteSubmissionDraft(accNo: String)

    suspend fun updateSubmissionDraft(
        accNo: String,
        content: String,
    )

    suspend fun createSubmissionDraft(content: String): WebSubmissionDraft
}

interface ExtSubmissionOperations {
    fun getExtSubmissions(extPageQuery: ExtPageQuery): ExtPage

    fun getExtSubmissionsPage(pageUrl: String): ExtPage

    fun getExtByAccNo(
        accNo: String,
        includeFileList: Boolean = false,
    ): ExtSubmission

    fun getReferencedFiles(filesUrl: String): ExtFileTable

    fun submitExtAsync(extSubmission: ExtSubmission)

    fun submitExt(extSubmission: ExtSubmission): ExtSubmission

    fun transferSubmission(
        accNo: String,
        target: StorageMode,
    )

    fun refreshSubmission(accNo: String): Pair<String, Int>

    fun releaseSubmission(
        accNo: String,
        releaseDate: Instant,
    ): Pair<String, Int>
}

interface PostProcessOperations {
    suspend fun postProcess(accNo: String)

    suspend fun recalculateStats(accNo: String)

    suspend fun copyPageTab(accNo: String)

    suspend fun indexInnerFiles(accNo: String)
}

interface PermissionOperations {
    suspend fun grantPermission(
        user: String,
        accNo: String,
        accessType: String,
    )

    suspend fun revokePermission(
        user: String,
        accNo: String,
        accessType: String,
    )
}

interface SubmitOperations {
    suspend fun submit(
        submission: Submission,
        format: SubmissionFormat = JSON,
        submitParameters: SubmitParameters? = null,
        register: OnBehalfParameters? = null,
    ): SubmissionResponse

    suspend fun submit(
        submission: String,
        format: SubmissionFormat = JSON,
        submitParameters: SubmitParameters? = null,
        register: OnBehalfParameters? = null,
    ): SubmissionResponse

    suspend fun submitAsync(
        submission: String,
        format: SubmissionFormat = JSON,
        submitParameters: SubmitParameters? = null,
        register: OnBehalfParameters? = null,
    ): SubmissionId

    suspend fun submitFromDraftAsync(draftKey: String)

    suspend fun submitFromDraft(
        draftKey: String,
        preferredSources: List<PreferredSource>? = null,
    ): SubmissionResponse
}

interface MultipartSubmitOperations {
    suspend fun submitMultipart(
        sub: String,
        format: SubmissionFormat,
        parameters: SubmitParameters,
        files: List<File> = emptyList(),
    ): SubmissionResponse

    suspend fun submitMultipart(
        sub: Submission,
        format: SubmissionFormat,
        parameters: SubmitParameters,
        files: List<File> = emptyList(),
    ): SubmissionResponse

    suspend fun submitMultipart(
        sub: File,
        parameters: SubmitParameters,
        files: List<File> = emptyList(),
    ): SubmissionResponse
}

interface MultipartAsyncSubmitOperations {
    suspend fun submitMultipartAsync(
        submission: String,
        format: SubmissionFormat,
        parameters: SubmitParameters,
        files: List<File> = emptyList(),
    ): SubmissionId

    suspend fun submitMultipartAsync(
        submission: Submission,
        format: SubmissionFormat,
        parameters: SubmitParameters,
    ): SubmissionId

    suspend fun submitMultipartAsync(
        submission: File,
        parameters: SubmitParameters,
        files: List<File> = emptyList(),
    ): SubmissionId

    suspend fun submitMultipartAsync(
        submissions: Map<String, String>,
        parameters: SubmitParameters,
        format: String,
        files: Map<String, List<File>>,
    ): List<SubmissionId>
}

interface SubmissionRequestOperations {
    suspend fun getSubmissionRequestStatus(
        accNo: String,
        version: Int,
    ): RequestStatus

    suspend fun archiveSubmissionRequest(
        accNo: String,
        version: Int,
    )
}
