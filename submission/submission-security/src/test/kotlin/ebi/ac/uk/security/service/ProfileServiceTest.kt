package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.DbUserGroup
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.GroupFolder
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class ProfileServiceTest(
    temporaryFolder: TemporaryFolder,
    @MockK
    private val privilegesService: IUserPrivilegesService,
    @MockK
    private val filesPersistenceService: SubmissionFilesPersistenceService,
) {
    private val environment = "env-test"
    private val filesDir = temporaryFolder.createDirectory("userFiles")
    private val ftpFilesDir = temporaryFolder.createDirectory("ftpFiles")
    private val testGroup = DbUserGroup("Test Group", "Test Group Description", "fd9f87b3-9de8-4036-be7a-3ac8cbc44ddd")

    private val testUser =
        DbUser(
            id = 3,
            email = "admin_user@ebi.ac.uk",
            fullName = "admin_user",
            secret = "69214a2f-f80b-4f33-86b7-26d3bd0453aa",
            orcid = "0000-0002-1825-0097",
            groups = mutableSetOf(testGroup),
            passwordDigest = "".toByteArray(),
            superuser = true,
            storageMode = StorageMode.NFS,
            notificationsEnabled = true,
        )

    private val testInstance =
        ProfileService(
            userFtpRootPath = environment,
            userFtpDirPath = ftpFilesDir.toPath(),
            nfsUserFilesDirPath = filesDir.toPath(),
            privilegesService = privilegesService,
            subFilesPersistenceService = filesPersistenceService,
        )

    @Test
    fun getUserProfile() {
        every { privilegesService.allowedCollections("admin_user@ebi.ac.uk", ADMIN) } returns listOf("BioCollection")

        val expectedUserFolder =
            NfsUserFolder(
                relativePath = Paths.get("69/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3"),
                path = Paths.get("$filesDir/69/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3"),
            )

        val expectedGroupFolder =
            GroupFolder(
                groupName = "Test Group",
                description = "Test Group Description",
                path = Paths.get("$filesDir/fd/9f87b3-9de8-4036-be7a-3ac8cbc44ddd-b0"),
            )

        val expectedUser =
            SecurityUser(
                id = 3,
                email = "admin_user@ebi.ac.uk",
                fullName = "admin_user",
                login = null,
                orcid = "0000-0002-1825-0097",
                secret = "69214a2f-f80b-4f33-86b7-26d3bd0453aa",
                superuser = true,
                userFolder = expectedUserFolder,
                groupsFolders = listOf(expectedGroupFolder),
                permissions = emptySet(),
                notificationsEnabled = true,
                adminCollections = listOf("BioCollection"),
            )

        val (user, token) = testInstance.getUserProfile(testUser, "a token")

        assertThat(user).usingRecursiveComparison().isEqualTo(expectedUser)
        assertThat(token).isEqualTo("a token")
    }
}
