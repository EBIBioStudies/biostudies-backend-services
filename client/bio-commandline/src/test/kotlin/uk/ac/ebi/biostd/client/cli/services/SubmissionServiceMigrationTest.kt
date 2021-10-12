package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import java.io.File

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionServiceMigrationTest(
    tempFolder: TemporaryFolder,
    @MockK private val sourceClient: BioWebClient,
    @MockK private val targetClient: BioWebClient
) {
    private val testInstance = SubmissionService()
    private val referencedFile = tempFolder.createFile("referenced.pdf")
    private val extSubmission = extSubmissionWithFileList()
    private val migrationRequest = MigrationRequest(
        accNo = "S-BSST1",
        source = "http://biostudy-prod",
        sourceUser = "manager",
        sourcePassword = "123456",
        target = "http://biostudy-bia",
        targetUser = "admin_user",
        targetPassword = "78910",
        targetOwner = null,
        async = false,
        tempFolder = tempFolder.root.absolutePath
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        setUpWebClients()
        setUpExtSubmissionClient()
    }

    @Test
    fun `migrate sync with no target Owner`() {
        val filesSlot = slot<List<File>>()

        every { targetClient.submitExt(extSubmission, capture(filesSlot)) } returns extSubmission

        testInstance.migrate(migrationRequest)

        val files = filesSlot.captured
        verifyMigration(files)
        verify(exactly = 1) { targetClient.submitExt(extSubmission, files) }
        verify(exactly = 0) { targetClient.submitExtAsync(extSubmission, files) }
    }

    @Test
    fun `migrate async with target Owner`() {
        val filesSlot = slot<List<File>>()
        val migrated = extSubmission.copy(owner = "newOwner")

        every { targetClient.submitExtAsync(migrated, capture(filesSlot)) } answers { nothing }

        testInstance.migrate(migrationRequest.copy(targetOwner = "newOwner", async = true))

        val files = filesSlot.captured
        verifyMigration(files)
        verify(exactly = 0) { targetClient.submitExt(migrated, files) }
        verify(exactly = 1) { targetClient.submitExtAsync(migrated, files) }
    }

    private fun verifyMigration(files: List<File>) {
        assertThat(files).hasSize(1)
        assertThat(files.first().name).isEqualTo("test-file-list")
        verify(exactly = 1) { sourceClient.getExtByAccNo("S-BSST1") }
    }

    private fun extSubmissionWithFileList(): ExtSubmission {
        val extSection = ExtSection(
            type = "Study",
            fileList = ExtFileList(
                "test-file-list",
                listOf(NfsFile(referencedFile.name, referencedFile, emptyList())),
                filesUrl = "/submissions/extended/S-BSST1/fileList/test-file-list/files"
            )
        )

        return basicExtSubmission.copy(section = extSection)
    }

    private fun setUpWebClients() {
        mockkObject(SecurityWebClient)
        every { create("http://biostudy-prod").getAuthenticatedClient("manager", "123456") } returns sourceClient
        every { create("http://biostudy-bia").getAuthenticatedClient("admin_user", "78910") } returns targetClient
    }

    private fun setUpExtSubmissionClient() {
        every { sourceClient.getExtByAccNo("S-BSST1") } returns extSubmission
        every {
            sourceClient.getReferencedFiles("/submissions/extended/S-BSST1/fileList/test-file-list/files")
        } returns ExtFileTable(NfsFile(referencedFile.name, referencedFile, emptyList()))
    }
}
