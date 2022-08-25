package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.dto.ReleaseRequestDto
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
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.model.Collection
import ebi.ac.uk.model.Group
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.WebSubmissionDraft
import java.io.File

interface SubmissionClient :
    SubmissionOperations,
    FilesOperations,
    GroupFilesOperations,
    MultipartSubmissionOperations,
    MultipartAsyncSubmissionOperations,
    GeneralOperations,
    DraftSubmissionOperations,
    ExtSubmissionOperations,
    PermissionOperations

typealias SubmissionResponse = ClientResponse<Submission>

data class SubmissionFilesConfig(
    val files: List<File>,
    val preferredSources: List<PreferredSource> = emptyList(),
)

interface FilesOperations {
    fun uploadFiles(files: List<File>, relativePath: String = EMPTY)
    fun uploadFile(file: File, relativePath: String = EMPTY)
    fun downloadFile(fileName: String, relativePath: String = EMPTY): File
    fun listUserFiles(relativePath: String = EMPTY): List<UserFile>
    fun deleteFile(fileName: String, relativePath: String = EMPTY)
    fun createFolder(folderName: String, relativePath: String = EMPTY)
}

interface GroupFilesOperations {
    fun uploadGroupFiles(groupName: String, files: List<File>, relativePath: String = EMPTY)
    fun downloadGroupFile(groupName: String, fileName: String, relativePath: String = EMPTY): File
    fun listGroupFiles(groupName: String, relativePath: String = EMPTY): List<UserFile>
    fun deleteGroupFile(groupName: String, fileName: String, relativePath: String = EMPTY)
    fun createGroupFolder(groupName: String, folderName: String, relativePath: String = EMPTY)
}

interface SubmissionOperations {
    fun submitSingle(
        submission: Submission,
        format: SubmissionFormat = JSON,
        register: RegisterConfig = NonRegistration
    ): SubmissionResponse

    fun submitSingle(
        submission: String,
        format: SubmissionFormat = JSON,
        register: RegisterConfig = NonRegistration
    ): SubmissionResponse

    fun submitSingleFromDraft(draftKey: String)

    fun submitAsync(submission: String, format: SubmissionFormat = JSON, register: RegisterConfig = NonRegistration)

    fun deleteSubmission(accNo: String)

    fun deleteSubmissions(submissions: List<String>)

    fun releaseSubmission(request: ReleaseRequestDto)

    fun getSubmissions(filter: Map<String, Any> = mapOf()): List<SubmissionDto>

    fun validateFileList(fileListPath: String, rootPath: String? = null, accNo: String? = null)
}

interface MultipartSubmissionOperations {
    fun submitSingle(
        submission: String,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    ): SubmissionResponse

    fun submitSingle(
        submission: Submission,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    ): SubmissionResponse

    fun submitSingle(
        submission: File,
        filesConfig: SubmissionFilesConfig,
        attrs: Map<String, String> = emptyMap(),
    ): SubmissionResponse
}

interface MultipartAsyncSubmissionOperations {
    fun asyncSubmitSingle(
        submission: String,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    )

    fun asyncSubmitSingle(
        submission: Submission,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    )

    fun asyncSubmitSingle(
        submission: File,
        filesConfig: SubmissionFilesConfig,
        attrs: Map<String, String> = emptyMap(),
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
    fun generateFtpLink(relPath: String)
    fun createGroup(groupName: String, groupDescription: String): UserGroupDto
    fun addUserInGroup(groupName: String, userName: String)
}

interface DraftSubmissionOperations {
    fun getAllSubmissionDrafts(limit: Int = 15, offset: Int = 0): List<WebSubmissionDraft>
    fun getSubmissionDraft(accNo: String): WebSubmissionDraft
    fun deleteSubmissionDraft(accNo: String)
    fun updateSubmissionDraft(accNo: String, content: String)
    fun createSubmissionDraft(content: String): WebSubmissionDraft
}

interface ExtSubmissionOperations {
    fun getExtSubmissions(extPageQuery: ExtPageQuery): ExtPage
    fun getExtSubmissionsPage(pageUrl: String): ExtPage
    fun getExtByAccNo(accNo: String, includeFileList: Boolean = false): ExtSubmission
    fun getReferencedFiles(filesUrl: String): ExtFileTable
    fun submitExtAsync(extSubmission: ExtSubmission)
    fun submitExt(extSubmission: ExtSubmission): ExtSubmission
    fun refreshSubmission(accNo: String): ExtSubmission
}

interface PermissionOperations {
    fun givePermissionToUser(user: String, accessTagName: String, accessType: String)
}
