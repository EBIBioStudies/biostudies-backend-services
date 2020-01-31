package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SubmissionSubmitterTest(
    @MockK private val filesSource: FilesSource,
    @MockK private val filesHandler: FilesHandler,
    @MockK private val submissionProcessor: SubmissionProcessor,
    @MockK private val persistenceContext: PersistenceContext,
    @MockK private val submissionRequest: SubmissionRequest,
    @MockK private val user: SecurityUser
) {
    private val submission = createBasicExtendedSubmission()
    private val savedSubmission = slot<ExtendedSubmission>()

    private val testInstance = SubmissionSubmitter(listOf(submissionProcessor), filesHandler)

    @BeforeEach
    fun beforeEach() {
        every { submissionRequest.files } answers { filesSource }
        every { submissionRequest.submission } answers { submission.asSubmission() }
        every { submissionRequest.user } answers { user }
        every { submissionRequest.method } answers { null }

        every { persistenceContext.deleteSubmissionDrafts(submission) } answers { nothing }
        every { persistenceContext.saveSubmission(capture(savedSubmission)) } answers { submission }
        every { filesHandler.processFiles(submission, filesSource) } answers { nothing }
        every { submissionProcessor.process(submission, persistenceContext) } answers { nothing }
        every { user.asUser() } answers { submission.user }
    }

    @Test
    fun submit() {
        testInstance.submit(submissionRequest, persistenceContext)

        assertThat(savedSubmission.captured.processingStatus).isEqualTo(PROCESSED)
        verify(exactly = 1) {
            persistenceContext.saveSubmission(submission)
            filesHandler.processFiles(submission, filesSource)
            submissionProcessor.process(submission, persistenceContext)
        }
    }
}
