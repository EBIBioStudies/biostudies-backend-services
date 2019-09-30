package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.TSV
import ac.uk.ebi.biostd.persistence.model.Submission as SubmissionDB
import ac.uk.ebi.biostd.persistence.util.SubmissionFilter
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.multipart.MultipartFile

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class SubmissionWebHandlerTest(
    temporaryFolder: TemporaryFolder,
    @MockK private val user: SecurityUser,
    @MockK private val pageTabReader: PageTabReader,
    @MockK private val multiPartAttachedFile: MultipartFile,
    @MockK private val submissionService: SubmissionService,
    @MockK private val tempFileGenerator: TempFileGenerator,
    @MockK private val multiPartSubmissionFile: MultipartFile,
    @MockK private val composedFileSource: ComposedFileSource,
    @MockK private val serializationService: SerializationService
) {
    private val submissionFilter = SubmissionFilter()
    private val submission = submission("S-TEST123") { }
    private val submissionFile = temporaryFolder.createFile("submission.tsv")
    private val submissionDB = SubmissionDB("S-TEST123").apply {
        title = "Test Submission"
        releaseTime = 123L
        creationTime = 123L
        modificationTime = 123L
    }

    @SpyK private var testInstance =
        SubmissionWebHandler(pageTabReader, submissionService, tempFileGenerator, serializationService)

    @BeforeEach
    fun beforeEach() {
        every { pageTabReader.read(submissionFile) } returns "Submission"
        every { serializationService.getSubmissionFormat(submissionFile) } returns TSV
        every { tempFileGenerator.asFile(multiPartSubmissionFile) } returns submissionFile
        every { submissionService.deleteSubmission("S-TEST123", user) } answers { nothing }
        every { submissionService.submit(submission, user, composedFileSource) } returns submission
        every { testInstance.getUserFilesSource(user, "Submission", TSV) } returns composedFileSource
        every { submissionService.getSubmissions(user, submissionFilter) } returns listOf(submissionDB)
        every { serializationService.deserializeSubmission("Submission", TSV, composedFileSource) } returns submission
        every {
            testInstance.getComposedFilesSource(user, arrayOf(multiPartAttachedFile), "Submission", TSV)
        } returns composedFileSource
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `submit with user directory and multipart files`() {
        testInstance.submit(user, arrayOf(multiPartAttachedFile), "Submission", TSV)

        verify(exactly = 1) {
            submissionService.submit(submission, user, composedFileSource)
            serializationService.deserializeSubmission("Submission", TSV, composedFileSource)
        }
    }

    @Test
    fun `submit only with user files`() {
        testInstance.submit(user, "Submission", TSV)

        verify(exactly = 1) {
            submissionService.submit(submission, user, composedFileSource)
            serializationService.deserializeSubmission("Submission", TSV, composedFileSource)
        }
    }

    @Test
    fun `submission as file with user directory and multipart files`() {
        testInstance.submit(user, multiPartSubmissionFile, arrayOf(multiPartAttachedFile))

        verify(exactly = 1) {
            pageTabReader.read(submissionFile)
            tempFileGenerator.asFile(multiPartSubmissionFile)
            serializationService.getSubmissionFormat(submissionFile)
            submissionService.submit(submission, user, composedFileSource)
            serializationService.deserializeSubmission("Submission", TSV, composedFileSource)
        }
    }

    @Test
    fun getSubmissions() {
        val submissions = testInstance.getSubmissions(user, submissionFilter)

        assertThat(submissions).hasSize(1)
        assertThat(submissions.first()).isEqualTo(SubmissionDto("S-TEST123", "Test Submission", 123L, 123L, 123L))

        verify(exactly = 1) { submissionService.getSubmissions(user, submissionFilter) }
    }

    @Test
    fun deleteSubmission() {
        testInstance.deleteSubmission("S-TEST123", user)
        verify(exactly = 1) { submissionService.deleteSubmission("S-TEST123", user) }
    }
}
