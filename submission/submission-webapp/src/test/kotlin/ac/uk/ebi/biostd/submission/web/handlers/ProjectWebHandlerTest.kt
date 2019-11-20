package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class ProjectWebHandlerTest(
    temporaryFolder: TemporaryFolder,
    @MockK private val user: SecurityUser,
    @MockK private val projectService: ProjectService,
    @MockK private val serializationService: SerializationService
) {
    private val project = submission("A-Project") { }
    private val projectFile = temporaryFolder.createFile("submission.tsv")
    private val testInstance = ProjectWebHandler(projectService, serializationService)

    @BeforeEach
    fun beforeEach() {
        every { projectService.submit(project, user) } returns project
        every { serializationService.deserializeSubmission(projectFile) } returns project
    }

    @Test
    fun submit() {
        testInstance.submit(user, projectFile)

        verify(exactly = 1) {
            projectService.submit(project, user)
            serializationService.deserializeSubmission(projectFile)
        }
    }
}
