package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
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
import org.springframework.web.multipart.MultipartFile

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class ProjectWebHandlerTest(
    temporaryFolder: TemporaryFolder,
    @MockK private val user: SecurityUser,
    @MockK private val multiPartFile: MultipartFile,
    @MockK private val pageTabReader: PageTabReader,
    @MockK private val projectService: ProjectService,
    @MockK private val tempFileGenerator: TempFileGenerator,
    @MockK private val serializationService: SerializationService
) {
    private val project = submission("A-Project") { }
    private val file = temporaryFolder.createFile("submission.tsv")
    private val testInstance = ProjectWebHandler(pageTabReader, projectService, tempFileGenerator, serializationService)

    @BeforeEach
    fun beforeEach() {
        every { pageTabReader.read(file) } returns "Project"
        every { projectService.submit(project, user) } returns project
        every { tempFileGenerator.asFile(multiPartFile) } returns file
        every { serializationService.getSubmissionFormat(file) } returns SubFormat.TSV
        every { serializationService.deserializeSubmission("Project", SubFormat.TSV) } returns project
    }

    @Test
    fun submit() {
        testInstance.submit(user, multiPartFile)

        verify(exactly = 1) {
            pageTabReader.read(file)
            projectService.submit(project, user)
            tempFileGenerator.asFile(multiPartFile)
            serializationService.getSubmissionFormat(file)
            serializationService.deserializeSubmission("Project", SubFormat.TSV)
        }
    }
}
