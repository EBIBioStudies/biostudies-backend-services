package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.api.security.RegisterResponse
import ebi.ac.uk.api.security.UserProfile
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.model.Submission
import org.springframework.http.ResponseEntity
import java.io.File

interface SubmissionClient :
    SubmissionOperations,
    FilesOperations,
    GroupFilesOperations,
    MultipartSubmissionOperations

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
    fun submitSingle(submission: Submission, format: SubmissionFormat = JSON): ResponseEntity<Submission>

    fun submitSingle(submission: String, format: SubmissionFormat = JSON): ResponseEntity<Submission>

    fun deleteSubmission(accNo: String)
}

interface MultipartSubmissionOperations {
    fun submitSingle(submission: String, format: SubmissionFormat, files: List<File>): ResponseEntity<Submission>

    fun submitSingle(submission: Submission, format: SubmissionFormat, files: List<File>): ResponseEntity<Submission>

    fun submitSingle(submission: File, files: List<File>): ResponseEntity<Submission>
}

interface SecurityOperations {
    fun getAuthenticatedClient(user: String, password: String): BioWebClient

    fun login(loginRequest: LoginRequest): UserProfile

    fun registerUser(registerRequest: RegisterRequest): RegisterResponse
}
