package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.TSV
import ac.uk.ebi.biostd.persistence.filter.SubmissionFilter
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.multipart.MultipartFile

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class SubmissionWebHandlerTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val userModel: User,
    @MockK private val user: SecurityUser,
    @MockK private val pageTabReader: PageTabReader,
    @MockK private val multiPartAttachedFile: MultipartFile,
    @MockK private val submissionService: SubmissionService,
    @MockK private val tempFileGenerator: TempFileGenerator,
    @MockK private val multiPartSubmissionFile: MultipartFile,
    @MockK private val composedFileSource: ComposedFileSource,
    @MockK private val serializationService: SerializationService
) {
    @SpyK private var testInstance =
        SubmissionWebHandler(pageTabReader, submissionService, tempFileGenerator, serializationService)

    @Nested
    inner class SubmissionOperationTest {
        private val submission = submission("S-TEST123") { }
        private val submissionFile = temporaryFolder.createFile("submission.tsv")

        @BeforeEach
        fun beforeEach() {
            every { submissionService.submit(submission, user, composedFileSource) } returns submission
            every {
                testInstance.getComposedFilesSource(user, arrayOf(multiPartAttachedFile), "Submission", TSV)
            } returns composedFileSource
            every {
                serializationService.deserializeSubmission("Submission", TSV, composedFileSource)
            } returns submission
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
            every { testInstance.getUserFilesSource(user, "Submission", TSV) } returns composedFileSource

            testInstance.submit(user, "Submission", TSV)

            verify(exactly = 1) {
                submissionService.submit(submission, user, composedFileSource)
                serializationService.deserializeSubmission("Submission", TSV, composedFileSource)
            }
        }

        @Test
        fun `submission as file with user directory and multipart files`() {
            every { pageTabReader.read(submissionFile) } returns "Submission"
            every { serializationService.getSubmissionFormat(submissionFile) } returns TSV
            every { tempFileGenerator.asFile(multiPartSubmissionFile) } returns submissionFile

            testInstance.submit(user, multiPartSubmissionFile, arrayOf(multiPartAttachedFile))

            verify(exactly = 1) {
                pageTabReader.read(submissionFile)
                tempFileGenerator.asFile(multiPartSubmissionFile)
                serializationService.getSubmissionFormat(submissionFile)
                submissionService.submit(submission, user, composedFileSource)
                serializationService.deserializeSubmission("Submission", TSV, composedFileSource)
            }
        }
    }

    @Nested
    inner class GetOperation {
        private val submissionFilter = SubmissionFilter()
        private val submission = ExtendedSubmission(submission("S-TEST123") { }, userModel)

        @Test
        fun getSubmissions() {
            every { submissionService.getSubmissions(user, submissionFilter) } returns listOf(submission)

            val submissions = testInstance.getSubmissions(user, submissionFilter)

            assertThat(submissions).hasSize(1)
            assertThat(submissions.first()).isEqualTo(submission)

            verify(exactly = 1) { submissionService.getSubmissions(user, submissionFilter) }
        }
    }

    @Nested
    inner class DeleteOperation {
        @Test
        fun deleteSubmission() {
            every { submissionService.deleteSubmission("S-TEST123", user) } answers { nothing }

            testInstance.deleteSubmission("S-TEST123", user)

            verify(exactly = 1) { submissionService.deleteSubmission("S-TEST123", user) }
        }
    }
}
