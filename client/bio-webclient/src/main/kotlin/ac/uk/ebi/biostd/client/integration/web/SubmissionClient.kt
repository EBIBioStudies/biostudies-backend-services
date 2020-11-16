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
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.UserProfile
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Group
import ebi.ac.uk.model.Project
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionDraft
import java.io.File

interface SubmissionClient :
    SubmissionOperations,
    FilesOperations,
    GroupFilesOperations,
    MultipartSubmissionOperations,
    GeneralOperations,
    DraftSubmissionOperations,
    ExtSubmissionOperations

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

    fun submitAsync(submission: String, format: SubmissionFormat = JSON, register: RegisterConfig = NonRegistration)

    fun refreshSubmission(accNo: String): SubmissionResponse
    fun deleteSubmission(accNo: String)
    fun getSubmissions(filter: Map<String, Any> = mapOf()): List<SubmissionDto>
}

interface MultipartSubmissionOperations {
    fun submitSingle(submission: String, format: SubmissionFormat, files: List<File>): SubmissionResponse
    fun submitSingle(submission: Submission, format: SubmissionFormat, files: List<File>): SubmissionResponse
    fun submitSingle(submission: File, files: List<File>, attrs: Map<String, String> = emptyMap()): SubmissionResponse
}

interface SecurityOperations {
    fun getAuthenticatedClient(user: String, password: String): BioWebClient
    fun getAuthenticatedClient(user: String, password: String, onBehalf: String): BioWebClient
    fun login(loginRequest: LoginRequest): UserProfile
    fun registerUser(registerRequest: RegisterRequest)
}

interface GeneralOperations {
    fun getGroups(): List<Group>
    fun getProjects(): List<Project>
}

interface DraftSubmissionOperations {
    fun getAllSubmissionDrafts(limit: Int = 15, offset: Int = 0): List<SubmissionDraft>
    fun getSubmissionDraft(accNo: String): SubmissionDraft
    fun deleteSubmissionDraft(accNo: String)
    fun updateSubmissionDraft(accNo: String, content: String)
    fun createSubmissionDraft(content: String): SubmissionDraft
}

interface ExtSubmissionOperations {
    fun getExtSubmissions(extPageQuery: ExtPageQuery): ExtPage

    fun getExtSubmissionsPage(pageUrl: String): ExtPage

    fun getExtByAccNo(accNo: String): ExtSubmission

    fun submitExt(extSubmission: ExtSubmission): ExtSubmission
}
