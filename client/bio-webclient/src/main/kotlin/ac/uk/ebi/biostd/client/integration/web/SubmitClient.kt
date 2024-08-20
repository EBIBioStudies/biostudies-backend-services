package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.dto.AcceptedSubmission
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
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.model.Collection
import ebi.ac.uk.model.Group
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionStat
import ebi.ac.uk.model.WebSubmissionDraft
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
    PermissionOperations,
    StatsOperations,
    SubmissionRequestOperations

typealias SubmissionResponse = ClientResponse<Submission>

interface FilesOperations {
    fun uploadFiles(
        files: List<File>,
        relativePath: String = EMPTY,
    )

    fun uploadFile(
        file: File,
        relativePath: String = EMPTY,
    )

    fun downloadFile(
        fileName: String,
        relativePath: String = EMPTY,
    ): File

    fun listUserFiles(relativePath: String = EMPTY): List<UserFile>

    fun deleteFile(
        fileName: String,
        relativePath: String = EMPTY,
    )

    fun createFolder(
        folderName: String,
        relativePath: String = EMPTY,
    )
}

interface GroupFilesOperations {
    fun uploadGroupFiles(
        groupName: String,
        files: List<File>,
        relativePath: String = EMPTY,
    )

    fun downloadGroupFile(
        groupName: String,
        fileName: String,
        relativePath: String = EMPTY,
    ): File

    fun listGroupFiles(
        groupName: String,
        relativePath: String = EMPTY,
    ): List<UserFile>

    fun deleteGroupFile(
        groupName: String,
        fileName: String,
        relativePath: String = EMPTY,
    )

    fun createGroupFolder(
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

interface GeneralOperations {
    fun getGroups(): List<Group>

    fun getCollections(): List<Collection>

    suspend fun generateFtpLinks(accNo: String)

    fun createGroup(
        groupName: String,
        groupDescription: String,
    ): UserGroupDto

    fun addUserInGroup(
        groupName: String,
        userName: String,
    )

    fun getProfile(): UserProfile
}

interface StatsOperations {
    fun getStatsByAccNo(accNo: String): List<SubmissionStat>

    fun getStatsByType(type: String): List<SubmissionStat>

    fun getStatsByTypeAndAccNo(
        type: String,
        accNo: String,
    ): SubmissionStat

    fun registerStat(stat: SubmissionStat): Unit

    fun registerStats(
        type: String,
        statsFile: File,
    ): List<SubmissionStat>

    fun incrementStats(
        type: String,
        statsFile: File,
    ): List<SubmissionStat>
}

interface DraftSubmissionOperations {
    fun getAllSubmissionDrafts(
        limit: Int = 15,
        offset: Int = 0,
    ): List<WebSubmissionDraft>

    fun getSubmissionDraft(accNo: String): WebSubmissionDraft

    fun deleteSubmissionDraft(accNo: String)

    fun updateSubmissionDraft(
        accNo: String,
        content: String,
    )

    fun createSubmissionDraft(content: String): WebSubmissionDraft
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

interface PermissionOperations {
    fun grantPermission(
        user: String,
        accNo: String,
        accessType: String,
    )
}

interface SubmitOperations {
    fun submit(
        submission: Submission,
        format: SubmissionFormat = JSON,
        submitParameters: SubmitParameters? = null,
        register: OnBehalfParameters? = null,
    ): SubmissionResponse

    fun submit(
        submission: String,
        format: SubmissionFormat = JSON,
        submitParameters: SubmitParameters? = null,
        register: OnBehalfParameters? = null,
    ): SubmissionResponse

    fun submitAsync(
        submission: String,
        format: SubmissionFormat = JSON,
        submitParameters: SubmitParameters? = null,
        register: OnBehalfParameters? = null,
    ): AcceptedSubmission

    fun submitFromDraftAsync(draftKey: String)

    fun submitFromDraft(
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
    fun submitMultipartAsync(
        submission: String,
        format: SubmissionFormat,
        parameters: SubmitParameters,
    ): AcceptedSubmission

    fun submitMultipartAsync(
        submission: Submission,
        format: SubmissionFormat,
        parameters: SubmitParameters,
    ): AcceptedSubmission

    fun submitMultipartAsync(
        submission: File,
        parameters: SubmitParameters,
    ): AcceptedSubmission
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
