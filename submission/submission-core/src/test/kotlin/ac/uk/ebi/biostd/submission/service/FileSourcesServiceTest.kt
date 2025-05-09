package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.ftp.FtpClient
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.io.sources.SourcesList
import ebi.ac.uk.paths.FolderType
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.security.integration.model.api.GroupFolder
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.io.builder.FilesSourceListBuilder
import java.nio.file.Paths

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FileSourcesServiceTest(
    tempFolder: TemporaryFolder,
    @MockK private val fireClient: FireClient,
    @MockK private val subFtp: FtpClient,
    @MockK private val ftpClient: FtpClient,
    @MockK private val filesRepo: SubmissionFilesPersistenceService,
    @MockK private val folderResolver: SubmissionFolderResolver,
) {
    private val tempFile = tempFolder.createFile("test.txt")
    private val filesFolder = tempFolder.createDirectory("files")
    private val subFolder = tempFolder.createDirectory("submissions")
    private val builder = FilesSourceListBuilder(folderResolver, fireClient, ftpClient, subFtp, filesRepo)
    private val testInstance = FileSourcesService(builder)
    private val extSubmission = basicExtSubmission.copy(storageMode = StorageMode.FIRE)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { folderResolver.getSubmisisonFolder(extSubmission, FolderType.NFS) } returns subFolder.toPath()
    }

    @Test
    fun `default submission sources with FIRE submission`() {
        val request =
            FileSourcesRequest(
                folderType = FolderType.NFS,
                onBehalfUser = onBehalfUser(),
                submitter = submitter(),
                files = listOf(tempFile),
                rootPath = "root-path",
                submission = extSubmission,
                preferredSources = emptyList(),
            )

        val fileSources = testInstance.submissionSources(request) as SourcesList

        val sources = fileSources.sources
        assertThat(sources).hasSize(7)
        assertThat(sources[0].description).isEqualTo("Provided Db files")
        assertThat(sources[1].description).isEqualTo("Request files [test.txt]")
        assertThat(sources[2].description).isEqualTo("admin_user@ebi.ac.uk user files in /root-path")
        assertThat(sources[3].description).isEqualTo("Group 'Test Group' files")
        assertThat(sources[4].description).isEqualTo("regular@ebi.ac.uk user files in /root-path")
        assertThat(sources[5].description).isEqualTo("Group 'Test Group' files")
        assertThat(sources[6].description).isEqualTo("Previous version files [File System]")
    }

    @Test
    fun `default submission sources with NFS submission`() {
        val request =
            FileSourcesRequest(
                folderType = FolderType.NFS,
                onBehalfUser = onBehalfUser(),
                submitter = submitter(),
                files = listOf(tempFile),
                rootPath = "root-path",
                submission = extSubmission,
                preferredSources = emptyList(),
            )

        val fileSources = testInstance.submissionSources(request) as SourcesList

        val sources = fileSources.sources
        assertThat(sources).hasSize(7)
        assertThat(sources[0].description).isEqualTo("Provided Db files")
        assertThat(sources[1].description).isEqualTo("Request files [test.txt]")
        assertThat(sources[2].description).isEqualTo("admin_user@ebi.ac.uk user files in /root-path")
        assertThat(sources[3].description).isEqualTo("Group 'Test Group' files")
        assertThat(sources[4].description).isEqualTo("regular@ebi.ac.uk user files in /root-path")
        assertThat(sources[5].description).isEqualTo("Group 'Test Group' files")
        assertThat(sources[6].description).isEqualTo("Previous version files [File System]")
    }

    @Test
    fun `default submission sources with no onBehalfUser`() {
        val request =
            FileSourcesRequest(
                folderType = FolderType.NFS,
                onBehalfUser = null,
                submitter = submitter(),
                files = listOf(tempFile),
                rootPath = "root-path",
                submission = extSubmission,
                preferredSources = emptyList(),
            )

        val fileSources = testInstance.submissionSources(request) as SourcesList

        val sources = fileSources.sources
        assertThat(sources).hasSize(5)
        assertThat(sources[0].description).isEqualTo("Provided Db files")
        assertThat(sources[1].description).isEqualTo("Request files [test.txt]")
        assertThat(sources[2].description).isEqualTo("admin_user@ebi.ac.uk user files in /root-path")
        assertThat(sources[3].description).isEqualTo("Group 'Test Group' files")
        assertThat(sources[4].description).isEqualTo("Previous version files [File System]")
    }

    @Test
    fun `submission sources with preferred sources`() {
        val request =
            FileSourcesRequest(
                folderType = FolderType.NFS,
                onBehalfUser = null,
                submitter = submitter(),
                files = null,
                rootPath = null,
                submission = extSubmission,
                preferredSources = listOf(SUBMISSION),
            )

        val fileSources = testInstance.submissionSources(request) as SourcesList

        val sources = fileSources.sources
        assertThat(sources).hasSize(2)
        assertThat(sources[0].description).isEqualTo("Provided Db files")
        assertThat(sources[1].description).isEqualTo("Previous version files [File System]")
    }

    private fun submitter(): SecurityUser {
        val userFolder =
            NfsUserFolder(
                relativePath = Paths.get("69/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3"),
                path = Paths.get("$filesFolder/69/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3"),
            )

        val groupFolder =
            GroupFolder(
                groupName = "Test Group",
                description = "Test Group Description",
                path = Paths.get("$filesFolder/fd/9f87b3-9de8-4036-be7a-3ac8cbc44ddd-b0"),
            )

        return SecurityUser(
            id = 3,
            email = "admin_user@ebi.ac.uk",
            fullName = "admin_user",
            login = null,
            orcid = "0000-0002-1825-0097",
            secret = "69214a2f-f80b-4f33-86b7-26d3bd0453aa",
            superuser = true,
            userFolder = userFolder,
            groupsFolders = listOf(groupFolder),
            permissions = emptySet(),
            notificationsEnabled = true,
        )
    }

    private fun onBehalfUser(): SecurityUser {
        val userFolder =
            NfsUserFolder(
                relativePath = Paths.get("43/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3"),
                path = Paths.get("$filesFolder/43/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3"),
            )

        val groupFolder =
            GroupFolder(
                groupName = "Test Group",
                description = "Test Group Description",
                path = Paths.get("$filesFolder/fd/9f87b3-9de8-4036-be7a-3ac8cbc44ddd-b0"),
            )

        return SecurityUser(
            id = 4,
            email = "regular@ebi.ac.uk",
            fullName = "regular_user",
            login = null,
            orcid = "1234-5678-9101-1121",
            secret = "98214a2f-f80b-4f33-86a4-26d3bd0453aa",
            superuser = true,
            userFolder = userFolder,
            groupsFolders = listOf(groupFolder),
            permissions = emptySet(),
            notificationsEnabled = true,
        )
    }
}
