package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.HttpServerErrorException

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmitProjectPermissionTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SubmitTest {
        @LocalServerPort
        private var serverPort: Int = 0

        private val project = tsv {
            line("Submission", "AProject")
            line()

            line("Project")
        }

        private val projectFile = tempFolder.createFile("a-project.tsv", project.toString())

        private lateinit var superUserWebClient: BioWebClient
        private lateinit var regularUserWebClient: BioWebClient

        @BeforeAll
        fun init() {
            superUserWebClient = getWebClient(serverPort, SuperUser)
            regularUserWebClient = getWebClient(serverPort, RegularUser)
        }

        @Test
        fun `create project with superuser`() {
            val response = superUserWebClient.submitProject(projectFile)

            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }

        @Test
        fun `create project with regular user`() {
            assertThatExceptionOfType(HttpServerErrorException::class.java).isThrownBy {
                regularUserWebClient.submitProject(projectFile)
            }
        }
    }
}
