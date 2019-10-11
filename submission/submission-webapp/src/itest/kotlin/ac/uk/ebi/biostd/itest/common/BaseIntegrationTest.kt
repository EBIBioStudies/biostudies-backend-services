package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.entities.TestUser
import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.io.File

internal open class BaseIntegrationTest(private val tempFolder: TemporaryFolder) {
    protected lateinit var basePath: String

    @BeforeAll
    fun init() {
        val dropbox = tempFolder.createDirectory("dropbox")
        val temp = tempFolder.createDirectory("tmp")
        basePath = tempFolder.root.absolutePath

        System.setProperty("app.basepath", tempFolder.root.absolutePath)
        System.setProperty("app.tempDirPath", temp.absolutePath)
        System.setProperty("app.security.filesDirPath", dropbox.absolutePath)
    }

    protected fun getWebClient(serverPort: Int, user: TestUser): BioWebClient {
        val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
        securityClient.registerUser(user.asRegisterRequest())

        return securityClient.getAuthenticatedClient(user.email, user.password)
    }

    protected fun submitString(
        webClient: BioWebClient,
        submission: String,
        format: SubmissionFormat = TSV,
        files: List<File> = emptyList()
    ) = assertSuccessfulResponse(webClient.submitSingle(submission, format, files))

    protected fun submitFile(webClient: BioWebClient, submission: File, files: List<File> = emptyList()) =
        assertSuccessfulResponse(webClient.submitSingle(submission, files))

    protected fun submitProject(webClient: BioWebClient, project: File) =
        assertSuccessfulResponse(webClient.submitProject(project))

    private fun <T> assertSuccessfulResponse(response: ResponseEntity<T>) {
        assertThat(response).isNotNull
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
    }
}
