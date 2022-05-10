package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.factory.TestSuperUser
import ac.uk.ebi.biostd.resolvers.TestBioUserResolver
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.validator.filelist.FileListValidator
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.constants.FILE_LIST_NAME
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class FileListResourceTest(
    @MockK private val sourceGenerator: SourceGenerator,
    @MockK private val fileListValidator: FileListValidator,
    @MockK private val onBehalfUtils: OnBehalfUtils
) {
    private val bioUserResolver = TestBioUserResolver()
    private val mvc = MockMvcBuilders
        .standaloneSetup(FileListResource(sourceGenerator, fileListValidator, onBehalfUtils))
        .setCustomArgumentResolvers(bioUserResolver)
        .build()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun validate(
        @MockK filesSource: FilesSource
    ) {
        val user = TestSuperUser.asSecurityUser()
        bioUserResolver.securityUser = user

        every { sourceGenerator.userSources(user, null, null) } returns filesSource
        every { fileListValidator.validateFileList("file-list", filesSource) } answers { nothing }

        mvc.post("/submissions/fileLists/validate") {
            param(FILE_LIST_NAME, "file-list")
        }.andExpect {
            status { isOk }
        }

        verify(exactly = 1) {
            sourceGenerator.userSources(user, null, null)
            fileListValidator.validateFileList("file-list", filesSource)
        }
    }
}
