package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.entities.TestUser
import ebi.ac.uk.dsl.Tsv
import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.springframework.http.HttpStatus
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

    protected fun submitTSV(webClient: BioWebClient, submission: Tsv, files: List<File> = emptyList()) {
        val response = webClient.submitSingle(submission.toString(), SubmissionFormat.TSV, files)
        assertThat(response).isNotNull
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }
}
