package ac.uk.ebi.biostd.files.web

import ac.uk.ebi.biostd.factory.TestSuperUser
import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.files.web.resources.UserFilesResource
import ac.uk.ebi.biostd.resolvers.TEST_USER_PATH
import ac.uk.ebi.biostd.resolvers.TestBioUserResolver
import ac.uk.ebi.biostd.resolvers.TestUserPathResolver
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class UserFilesResourceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val filesMapper: FilesMapper,
    @MockK private val fileManager: UserFilesService
) {
    private val bioUserResolver = TestBioUserResolver()
    private val userPathResource = TestUserPathResolver()
    private val testInstance = UserFilesResource(filesMapper, fileManager)
    private val mvc = MockMvcBuilders
        .standaloneSetup(testInstance)
        .setCustomArgumentResolvers(bioUserResolver, userPathResource)
        .build()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `download file`() {
        val file = tempFolder.createFile("test.txt", "test content")
        val user = slot<SecurityUser>()
        bioUserResolver.securityUser = TestSuperUser.asSecurityUser()

        every { fileManager.getFile(capture(user), TEST_USER_PATH, file.name) } returns file

        mvc.get("/files/user/folder") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            param("fileName", file.name)
        }.andExpect {
            status { isOk }
            header { string("Content-Disposition", "inline; filename=\"test.txt\"") }
        }
    }
}
