package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ebi.ac.uk.model.Submission
import org.springframework.http.ResponseEntity
import java.io.File

interface SubmissionClient {

    fun submitSingle(submission: Submission, format: SubmissionFormat = SubmissionFormat.JSON): ResponseEntity<Submission>

    fun submitSingle(submission: String, format: SubmissionFormat = SubmissionFormat.JSON): ResponseEntity<Submission>

    fun uploadFiles(files: List<File>, relativePath: String = "")
}
