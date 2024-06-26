package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.dto.AcceptedSubmission
import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ebi.ac.uk.api.ClientResponse
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.dto.NonRegistration
import ebi.ac.uk.api.dto.RegisterConfig
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

data class SubmissionFilesConfig(
    val files: List<File>,
    val storageMode: StorageMode,
    val preferredSources: List<PreferredSource> = emptyList(),
)

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
    fun deleteSubmission(accNo: String)

    fun deleteSubmissions(submissions: List<String>)

    fun getSubmissions(filter: Map<String, Any> = mapOf()): List<SubmissionDto>

    fun validateFileList(
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
    fun submitSingle(
        submission: Submission,
        format: SubmissionFormat = JSON,
        storageMode: StorageMode? = null,
        register: RegisterConfig = NonRegistration,
    ): SubmissionResponse

    fun submitSingle(
        submission: String,
        format: SubmissionFormat = JSON,
        storageMode: StorageMode? = null,
        register: RegisterConfig = NonRegistration,
    ): SubmissionResponse

    fun submitSingleFromDraftAsync(draftKey: String)

    fun submitSingleFromDraft(
        draftKey: String,
        preferredSources: List<PreferredSource>? = null,
    ): SubmissionResponse

    fun submitAsync(
        submission: String,
        format: SubmissionFormat = JSON,
        storageMode: StorageMode? = null,
        register: RegisterConfig = NonRegistration,
    ): AcceptedSubmission
}

interface MultipartSubmitOperations {
    suspend fun submitSingle(
        sub: String,
        format: SubmissionFormat,
        config: SubmissionFilesConfig,
    ): SubmissionResponse

    suspend fun submitSingle(
        sub: Submission,
        format: SubmissionFormat,
        config: SubmissionFilesConfig,
    ): SubmissionResponse

    suspend fun submitSingle(
        sub: File,
        config: SubmissionFilesConfig,
        attrs: Map<String, String> = emptyMap(),
    ): SubmissionResponse
}

interface MultipartAsyncSubmitOperations {
    fun asyncSubmitSingle(
        submission: String,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    ): AcceptedSubmission

    fun asyncSubmitSingle(
        submission: Submission,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    ): AcceptedSubmission

    fun asyncSubmitSingle(
        submission: File,
        filesConfig: SubmissionFilesConfig,
        attrs: Map<String, String> = emptyMap(),
    ): AcceptedSubmission
}

interface SubmissionRequestOperations {
    fun getSubmissionRequestStatus(
        accNo: String,
        version: Int,
    ): RequestStatus
}
