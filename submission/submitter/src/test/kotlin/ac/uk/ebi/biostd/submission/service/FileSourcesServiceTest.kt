package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.helpers.FireFilesSourceFactory
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PreferredSource.FIRE
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.MagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FileSourcesServiceTest(
    tempFolder: TemporaryFolder,
    @MockK private val fireSource: FilesSource,
    @MockK private val subFireSource: FilesSource,
    @MockK private val props: ApplicationProperties,
    @MockK private val extSubmission: ExtSubmission,
    @MockK private val fireSourceFactory: FireFilesSourceFactory,
) {
    private val tempFile = tempFolder.createFile("test.txt")
    private val filesFolder = tempFolder.createDirectory("files")
    private val subFolder = tempFolder.createDirectory("submissions")
    private val testInstance = FileSourcesService(props, fireSourceFactory)

    @BeforeAll
    fun beforeAll() {
        every { extSubmission.accNo } returns "S-BSST1"
        every { extSubmission.relPath } returns "S-BSST/001/S-BSST1"
        every { props.submissionPath } returns subFolder.absolutePath
        every { fireSourceFactory.createFireSource() } returns fireSource
        every {
            fireSourceFactory.createSubmissionFireSource("S-BSST1", Paths.get("S-BSST/001/S-BSST1/Files"))
        } returns subFireSource
    }

    @Test
    fun `default submission sources with FIRE enabled`() {
        every { props.persistence.enableFire } returns true

        val request = FileSourcesRequest(
            onBehalfUser = onBehalfUser(),
            submitter = submitter(),
            files = listOf(tempFile),
            rootPath = "root-path",
            submission = extSubmission,
            preferredSources = emptyList()
        )

        val fileSources = testInstance.submissionSources(request)

        val sources = fileSources.sources
        assertThat(sources).hasSize(8)
        assertThat(sources[0].description).isEqualTo("Request files [test.txt]")
        assertThat(sources[1].description).isEqualTo("admin_user@ebi.ac.uk user files in /root-path")
        assertThat(sources[2].description).isEqualTo("Group 'Test Group' files")
        assertThat(sources[3].description).isEqualTo("regular@ebi.ac.uk user files in /root-path")
        assertThat(sources[4].description).isEqualTo("Group 'Test Group' files")
        assertThat(sources[5]).isEqualTo(subFireSource)
        assertThat(sources[6].description).isEqualTo("Submission S-BSST1 previous version files")
        assertThat(sources[7]).isEqualTo(fireSource)
    }

    @Test
    fun `default submission sources with FIRE disabled and no onBehalfUser`() {
        every { props.persistence.enableFire } returns false

        val request = FileSourcesRequest(
            onBehalfUser = null,
            submitter = submitter(),
            files = listOf(tempFile),
            rootPath = "root-path",
            submission = extSubmission,
            preferredSources = emptyList()
        )

        val fileSources = testInstance.submissionSources(request)

        val sources = fileSources.sources
        assertThat(sources).hasSize(5)
        assertThat(sources[0].description).isEqualTo("Request files [test.txt]")
        assertThat(sources[1].description).isEqualTo("admin_user@ebi.ac.uk user files in /root-path")
        assertThat(sources[2].description).isEqualTo("Group 'Test Group' files")
        assertThat(sources[3]).isEqualTo(subFireSource)
        assertThat(sources[4].description).isEqualTo("Submission S-BSST1 previous version files")
    }

    @Test
    fun `submission sources with preferred sources`() {
        every { props.persistence.enableFire } returns true

        val request = FileSourcesRequest(
            onBehalfUser = null,
            submitter = submitter(),
            files = null,
            rootPath = null,
            submission = extSubmission,
            preferredSources = listOf(FIRE, SUBMISSION)
        )

        val fileSources = testInstance.submissionSources(request)

        val sources = fileSources.sources
        assertThat(sources).hasSize(3)
        assertThat(sources[0]).isEqualTo(fireSource)
        assertThat(sources[1]).isEqualTo(subFireSource)
        assertThat(sources[2].description).isEqualTo("Submission S-BSST1 previous version files")
    }

    private fun submitter(): SecurityUser {
        val userFolder = MagicFolder(
            relativePath = Paths.get("69/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3"),
            path = Paths.get("$filesFolder/69/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3")
        )

        val groupFolder = GroupMagicFolder(
            groupName = "Test Group",
            description = "Test Group Description",
            path = Paths.get("$filesFolder/fd/9f87b3-9de8-4036-be7a-3ac8cbc44ddd-b0")
        )

        return SecurityUser(
            id = 3,
            email = "admin_user@ebi.ac.uk",
            fullName = "admin_user",
            login = null,
            orcid = "0000-0002-1825-0097",
            secret = "69214a2f-f80b-4f33-86b7-26d3bd0453aa",
            superuser = true,
            magicFolder = userFolder,
            groupsFolders = listOf(groupFolder),
            permissions = emptySet(),
            notificationsEnabled = true
        )
    }

    private fun onBehalfUser(): SecurityUser {
        val userFolder = MagicFolder(
            relativePath = Paths.get("43/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3"),
            path = Paths.get("$filesFolder/43/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3")
        )

        val groupFolder = GroupMagicFolder(
            groupName = "Test Group",
            description = "Test Group Description",
            path = Paths.get("$filesFolder/fd/9f87b3-9de8-4036-be7a-3ac8cbc44ddd-b0")
        )

        return SecurityUser(
            id = 4,
            email = "regular@ebi.ac.uk",
            fullName = "regular_user",
            login = null,
            orcid = "1234-5678-9101-1121",
            secret = "98214a2f-f80b-4f33-86a4-26d3bd0453aa",
            superuser = true,
            magicFolder = userFolder,
            groupsFolders = listOf(groupFolder),
            permissions = emptySet(),
            notificationsEnabled = true
        )
    }
}
