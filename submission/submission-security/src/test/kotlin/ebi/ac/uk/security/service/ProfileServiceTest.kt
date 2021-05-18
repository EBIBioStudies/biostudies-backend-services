package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.model.UserGroup
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.MagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class)
class ProfileServiceTest(temporaryFolder: TemporaryFolder) {
    private val filesDir = temporaryFolder.root.toPath()
    private val testGroup = UserGroup("Test Group", "Test Group Description", "fd9f87b3-9de8-4036-be7a-3ac8cbc44ddd")

    private val testUser = DbUser(
        id = 3,
        email = "admin_user@ebi.ac.uk",
        fullName = "admin_user",
        secret = "69214a2f-f80b-4f33-86b7-26d3bd0453aa",
        groups = mutableSetOf(testGroup),
        passwordDigest = "".toByteArray(),
        superuser = true,
        notificationsEnabled = true
    )

    private val testInstance = ProfileService(filesDir)

    @Test
    fun getUserProfile() {
        val expectedUserFolder = MagicFolder(
            relativePath = Paths.get("69/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3"),
            path = Paths.get("$filesDir/69/214a2f-f80b-4f33-86b7-26d3bd0453aa-a3")
        )

        val expectedGroupFolder = GroupMagicFolder(
            groupName = "Test Group",
            description = "Test Group Description",
            path = Paths.get("$filesDir/fd/9f87b3-9de8-4036-be7a-3ac8cbc44ddd-b0")
        )

        val expectedUser = SecurityUser(
            id = 3,
            email = "admin_user@ebi.ac.uk",
            fullName = "admin_user",
            login = null,
            secret = "69214a2f-f80b-4f33-86b7-26d3bd0453aa",
            superuser = true,
            magicFolder = expectedUserFolder,
            groupsFolders = listOf(expectedGroupFolder),
            permissions = emptySet(),
            notificationsEnabled = true
        )

        val (user, token) = testInstance.getUserProfile(testUser, "a token")

        assertThat(user).isEqualToComparingFieldByField(expectedUser)
        assertThat(token).isEqualTo("a token")
    }
}
