package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.entities.TestUser
import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.junit.jupiter.api.BeforeAll

internal open class BaseIntegrationTest(
    private val tempFolder: TemporaryFolder
) {
    protected lateinit var submissionPath: String
    protected lateinit var ftpPath: String

    @BeforeAll
    fun init() {
        val temp = tempFolder.createDirectory("tmp")
        submissionPath = "${tempFolder.root.absolutePath}/submission"
        ftpPath = "${tempFolder.root.absolutePath}/ftpPath"

        System.setProperty("app.submissionPath", submissionPath)
        System.setProperty("app.ftpPath", ftpPath)
        System.setProperty("app.tempDirPath", temp.absolutePath)

        val dropbox = tempFolder.createDirectory("dropbox")
        val magicFolders = tempFolder.createDirectory("magic")
        System.setProperty("app.security.filesDirPath", dropbox.absolutePath)
        System.setProperty("app.security.magicDirPath", magicFolders.absolutePath)
    }

    protected fun getWebClient(serverPort: Int, user: TestUser): BioWebClient {
        val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
        return securityClient.getAuthenticatedClient(user.email, user.password)
    }

    protected fun createUser(testUser: TestUser, serverPort: Int) {
        SecurityWebClient.create("http://localhost:$serverPort").registerUser(testUser.asRegisterRequest())
    }
}
