package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.dto.ExtPage
import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ebi.ac.uk.api.ClientResponse
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.dto.NonRegistration
import ebi.ac.uk.api.dto.RegisterConfig
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.api.security.CheckUserRequest
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.UserProfile
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
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

interface FilesOperations {
    fun uploadFiles(files: List<File>, relativePath: String = EMPTY)
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

    fun refreshSubmission(accNo: String): SubmissionResponse
    fun deleteSubmission(accNo: String)
    fun deleteSubmissions(submissions: List<String>)
    fun getSubmissions(filter: Map<String, Any> = mapOf()): List<SubmissionDto>
}

interface MultipartSubmissionOperations {
    fun submitSingle(submission: String, format: SubmissionFormat, files: List<File>): SubmissionResponse
    fun submitSingle(submission: Submission, format: SubmissionFormat, files: List<File>): SubmissionResponse
    fun submitSingle(submission: File, files: List<File>, attrs: Map<String, String> = emptyMap()): SubmissionResponse
}

interface MultipartAsyncSubmissionOperations {
    fun asyncSubmitSingle(submission: String, format: SubmissionFormat, files: List<File>)
    fun asyncSubmitSingle(submission: Submission, format: SubmissionFormat, files: List<File>)
    fun asyncSubmitSingle(submission: File, files: List<File>, attrs: Map<String, String> = emptyMap())
}

interface SecurityOperations {
    fun getAuthenticatedClient(user: String, password: String, onBehalf: String? = null): BioWebClient
    fun login(loginRequest: LoginRequest): UserProfile
    fun registerUser(registerRequest: RegisterRequest)
    fun checkUser(checkUserRequest: CheckUserRequest)
}

interface GeneralOperations {
    fun getGroups(): List<Group>
    fun getCollections(): List<Collection>
    fun generateFtpLink(relPath: String)
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
    fun getExtByAccNo(accNo: String): ExtSubmission
    fun getReferencedFiles(filesUrl: String): ExtFileTable
    fun submitExt(extSubmission: ExtSubmission, fileLists: List<File> = emptyList()): ExtSubmission
}

interface PermissionOperations {
    fun givePermissionToUser(user: String, accessTagName: String, accessType: String)
}
