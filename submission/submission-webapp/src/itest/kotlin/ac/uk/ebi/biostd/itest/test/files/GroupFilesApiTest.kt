package ac.uk.ebi.biostd.itest.test.files

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType
import ebi.ac.uk.io.ext.createFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Paths

const val testGroupName = "Bio-test-group-api"
const val testGroupDescription = "Bio-test-group for api test"

@Import(SubmitterConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GroupFilesApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)

        webClient = getWebClient(serverPort, SuperUser)
        webClient.addUserInGroup(webClient.createGroup(testGroupName, testGroupDescription).name, SuperUser.email)
    }

    @Test
    fun `18,1 - upload download delete file and retrieve in user root folder`() {
        val file = tempFolder.createFile("FileList1.txt", "An example content")
        webClient.uploadGroupFiles(testGroupName, listOf(file))

        val files = webClient.listGroupFiles(testGroupName)
        assertThat(files).hasSize(1)
        assertFile(files.first(), webClient.downloadGroupFile(testGroupName, file.name), file, "")

        webClient.deleteGroupFile(testGroupName, "FileList1.txt")
        assertThat(webClient.listGroupFiles(testGroupName)).isEmpty()
    }

    @Test
    fun `18,2 - upload download delete file and retrieve in user folder`() {
        val file = tempFolder.createFile("FileList1.txt", "An example content")

        webClient.uploadGroupFiles(testGroupName, listOf(file), relativePath = "test-folder")
        val files = webClient.listGroupFiles(testGroupName, relativePath = "test-folder")

        assertThat(files).hasSize(1)
        assertFile(
            files.first(),
            webClient.downloadGroupFile(testGroupName, file.name, relativePath = "test-folder"),
            file,
            path = "test-folder"
        )
        webClient.deleteGroupFile(testGroupName, "FileList1.txt", relativePath = "test-folder")
        assertThat(webClient.listGroupFiles(testGroupName, relativePath = "test-folder")).isEmpty()

        webClient.deleteGroupFile(testGroupName, "test-folder")
    }

    private fun assertFile(resultFile: UserFile, downloadFile: File, file: File, path: String) {
        assertThat(resultFile.name).isEqualTo(file.name)
        assertThat(resultFile.type).isEqualTo(UserFileType.FILE)
        assertThat(resultFile.path).isEqualTo(Paths.get("groups").resolve(testGroupName).resolve(path).toString())
        assertThat(resultFile.size).isEqualTo(file.length())
        assertThat(file).hasContent(downloadFile.readText())
    }
}
