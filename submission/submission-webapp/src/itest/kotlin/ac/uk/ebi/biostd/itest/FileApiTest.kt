package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.common.config.SubmitterConfig
import ac.uk.ebi.biostd.files.FileConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.GenericUser
import ebi.ac.uk.api.UserFileType
import ebi.ac.uk.api.security.RegisterRequest
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
internal class FileApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {

    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [SubmitterConfig::class, PersistenceConfig::class, FileConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class FilesTest {

        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
            securityClient.registerUser(RegisterRequest(
                GenericUser.email,
                GenericUser.username,
                GenericUser.password
            ))
            webClient = securityClient.getAuthenticatedClient(GenericUser.username, GenericUser.password)
        }

        @Test
        fun `upload file and retrieve in user folder`() {
            val file = tempFolder.createFile("LibraryFile1.txt")

            webClient.uploadFiles(listOf(file))

            val files = webClient.listUserFiles()
            assertThat(files).hasSize(1)

            val resultFile = files.first()
            assertThat(resultFile.name).isEqualTo(file.name)
            assertThat(resultFile.type).isEqualTo(UserFileType.FILE)

            webClient.deleteFile("LibraryFile1.txt")
        }

        @Test
        fun `upload file in directory and retrieve in user folder`() {
            val file = tempFolder.createFile("AnotherFile.txt")

            webClient.createFolder("test_folder")
            webClient.uploadFiles(listOf(file), relativePath = "test_folder")

            val files = webClient.listUserFiles(relativePath = "test_folder")
            assertThat(files).hasSize(1)

            val resultFile = files.first()
            assertThat(resultFile.name).isEqualTo(file.name)
            assertThat(resultFile.type).isEqualTo(UserFileType.FILE)

            webClient.deleteFile("test_folder")
        }
    }
}
