package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import ebi.ac.uk.io.ext.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class UserFilesServiceTest(
    private val tempFolder: TemporaryFolder,
    @param:MockK private val bioWebClient: BioWebClient,
) {
    private val testInstance = UserFilesService()
    private val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockkObject(SecurityWebClient)
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } returns bioWebClient
    }

    @Test
    fun `upload single user file`() =
        runTest {
            val file = tempFolder.createFile(FILE_NAME)

            coEvery { bioWebClient.uploadFile(file, REL_PATH) } answers { nothing }

            testInstance.httpUpload(securityConfig, file, REL_PATH)

            coVerify(exactly = 1) { bioWebClient.uploadFile(file, REL_PATH) }
        }

    @Test
    fun `upload directory`() =
        runTest {
            val dir = tempFolder.createDirectory(DIR_NAME)
            val file = dir.createFile(FILE_NAME)

            coEvery { bioWebClient.uploadFile(file, REL_PATH) } answers { nothing }

            testInstance.httpUpload(securityConfig, dir, REL_PATH)

            coVerify(exactly = 1) { bioWebClient.uploadFile(file, REL_PATH) }
        }

    @Test
    fun `delete user files`() =
        runTest {
            coEvery { bioWebClient.deleteFile(FILE_NAME, REL_PATH) } answers { nothing }

            testInstance.deleteUserFiles(securityConfig, FILE_NAME, REL_PATH)

            coVerify(exactly = 1) { bioWebClient.deleteFile(FILE_NAME, REL_PATH) }
        }

    @Test
    fun `datamover starts cluster upload and waits until completed`() =
        runTest {
            val file = tempFolder.createFile(FILE_NAME)
            coEvery { bioWebClient.uploadFilesThroughCluster(file, REL_PATH) } returns JOB_ID
            coEvery { bioWebClient.getClusterJobStatus(JOB_ID) } returnsMany listOf("PENDING", "RUNNING", "COMPLETED")

            testInstance.datamoverUpload(securityConfig, file, REL_PATH)

            coVerify(exactly = 1) { bioWebClient.uploadFilesThroughCluster(file, REL_PATH) }
            coVerify(exactly = 3) { bioWebClient.getClusterJobStatus(JOB_ID) }
            coVerify(exactly = 0) { bioWebClient.uploadFile(any(), any()) }
        }

    private companion object {
        private const val DIR_NAME = "test-dir"
        private const val FILE_NAME = "file.txt"
        private const val JOB_ID = "12345"
        private const val REL_PATH = "relPath"
        private const val PASSWORD = "password"
        private const val SERVER = "server"
        private const val USER = "user"
    }
}
