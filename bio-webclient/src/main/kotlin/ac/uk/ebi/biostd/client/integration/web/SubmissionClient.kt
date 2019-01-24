package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.model.Submission
import org.springframework.http.ResponseEntity
import java.io.File

interface SubmissionClient : SubmissionOperations, FilesOperations, GroupFilesOperations

interface FilesOperations {

    fun uploadFiles(files: List<File>, relativePath: String = EMPTY)

    fun listUserFiles(relativePath: String = EMPTY): List<UserFile>

    fun deleteFile(fileName: String, relativePath: String = EMPTY)

    fun createFolder(folderName: String, relativePath: String = EMPTY)
}

interface GroupFilesOperations {

    fun uploadGroupFiles(groupName: String, files: List<File>, relativePath: String = EMPTY)

    fun listGroupFiles(groupName: String, relativePath: String = EMPTY): List<UserFile>

    fun deleteGroupFile(groupName: String, fileName: String, relativePath: String = EMPTY)

    fun createGroupFolder(groupName: String, folderName: String, relativePath: String = EMPTY)
}

interface SubmissionOperations {

    fun submitSingle(submission: Submission, format: SubmissionFormat = JSON): ResponseEntity<Submission>

    fun submitSingle(submission: String, format: SubmissionFormat = JSON): ResponseEntity<Submission>
}
