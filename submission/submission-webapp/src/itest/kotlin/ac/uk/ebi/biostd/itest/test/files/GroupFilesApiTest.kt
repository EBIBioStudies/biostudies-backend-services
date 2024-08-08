package ac.uk.ebi.biostd.itest.test.files

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.submission.config.SubmitterConfig
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType
import ebi.ac.uk.io.ext.createFile
import kotlinx.coroutines.test.runTest
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

const val TEST_GROUP_NAME = "Bio-test-group-api"
const val TEST_GROUP_DESCRIPTION = "Bio-test-group for api test"

@Import(SubmitterConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GroupFilesApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runTest {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)

            val group = webClient.createGroup(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION)
            webClient.addUserInGroup(group.name, SuperUser.email)
        }

    @Test
    fun `18-1 upload download delete file and retrieve in user root folder`() {
        val file = tempFolder.createFile("FileList1.txt", "An example content")
        webClient.uploadGroupFiles(TEST_GROUP_NAME, listOf(file))

        val files = webClient.listGroupFiles(TEST_GROUP_NAME)
        assertThat(files).hasSize(1)
        assertFile(files.first(), webClient.downloadGroupFile(TEST_GROUP_NAME, file.name), file, "")

        webClient.deleteGroupFile(TEST_GROUP_NAME, "FileList1.txt")
        assertThat(webClient.listGroupFiles(TEST_GROUP_NAME)).isEmpty()
    }

    @Test
    fun `18-2 upload download delete file and retrieve in user folder`() {
        val file = tempFolder.createFile("FileList1.txt", "An example content")

        webClient.uploadGroupFiles(TEST_GROUP_NAME, listOf(file), relativePath = "test-folder")
        val files = webClient.listGroupFiles(TEST_GROUP_NAME, relativePath = "test-folder")

        assertThat(files).hasSize(1)
        assertFile(
            files.first(),
            webClient.downloadGroupFile(TEST_GROUP_NAME, file.name, relativePath = "test-folder"),
            file,
            path = "test-folder",
        )
        webClient.deleteGroupFile(TEST_GROUP_NAME, "FileList1.txt", relativePath = "test-folder")
        assertThat(webClient.listGroupFiles(TEST_GROUP_NAME, relativePath = "test-folder")).isEmpty()

        webClient.deleteGroupFile(TEST_GROUP_NAME, "test-folder")
    }

    private fun assertFile(
        resultFile: UserFile,
        downloadFile: File,
        file: File,
        path: String,
    ) {
        assertThat(resultFile.name).isEqualTo(file.name)
        assertThat(resultFile.type).isEqualTo(UserFileType.FILE)
        assertThat(resultFile.path).isEqualTo(
            Paths
                .get("groups")
                .resolve(TEST_GROUP_NAME)
                .resolve(path)
                .toString(),
        )
        assertThat(resultFile.size).isEqualTo(file.length())
        assertThat(file).hasContent(downloadFile.readText())
    }
}
