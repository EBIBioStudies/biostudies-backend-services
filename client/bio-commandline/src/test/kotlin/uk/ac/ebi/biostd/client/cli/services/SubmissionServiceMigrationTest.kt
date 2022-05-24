package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.extended.serialization.service.createExtFileList

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
        fileMode = COPY,
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
    fun `migrate sync`() {
        every { targetClient.submitExt(extSubmission) } returns extSubmission

        testInstance.migrate(migrationRequest)

        verify(exactly = 1) { targetClient.submitExt(extSubmission) }
        verify(exactly = 0) { targetClient.submitExtAsync(extSubmission) }
    }

    @Test
    fun `migrate async`() {
        val expected = extSubmission.copy(owner = "newOwner")
        every { targetClient.submitExtAsync(expected) } answers { nothing }

        testInstance.migrate(migrationRequest.copy(targetOwner = "newOwner", async = true))

        verify(exactly = 0) { targetClient.submitExt(expected) }
        verify(exactly = 1) { targetClient.submitExtAsync(expected) }
    }

    private fun extSubmissionWithFileList(): ExtSubmission {
        val extSection = ExtSection(
            type = "Study",
            fileList = ExtFileList(
                "test-file-list",
                file = createExtFileList(),
                filesUrl = "/submissions/extended/S-BSST1/referencedFiles/test-file-list",
                pageTabFiles = listOf(
                    NfsFile(
                        "filePath",
                        "relPath",
                        referencedFile,
                        referencedFile.absolutePath,
                        referencedFile.md5(),
                        referencedFile.size(),
                        emptyList()
                    )
                )
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
        every { sourceClient.getExtByAccNo("S-BSST1", true) } returns extSubmission
        every {
            sourceClient.getReferencedFiles("/submissions/extended/S-BSST1/referencedFiles/test-file-list")
        } returns ExtFileTable(
            NfsFile(
                filePath = "folder/referenced.pdf",
                relPath = "Files/folder/referenced.pdf",
                file = referencedFile,
                referencedFile.absolutePath,
                referencedFile.md5(),
                referencedFile.size(),
                attributes = emptyList()
            )
        )
    }
}
