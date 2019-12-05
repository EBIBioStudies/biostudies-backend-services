package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.constants.Processed
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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
    @MockK private val persistenceContext: PersistenceContext
) {
    private val submission = createBasicExtendedSubmission()
    private val testInstance =
        SubmissionSubmitter(listOf(submissionProcessor), filesHandler)

    @BeforeEach
    fun beforeEach() {
        every { persistenceContext.deleteSubmissionDrafts(submission) } answers { nothing }
        every { persistenceContext.saveSubmission(submission) } answers { submission }
        every { filesHandler.processFiles(submission, filesSource) } answers { nothing }
        every { submissionProcessor.process(submission, persistenceContext) } answers { nothing }
    }

    @Test
    fun submit() {
        testInstance.submit(submission, filesSource, persistenceContext)
        assertThat(submission.processingStatus).isEqualTo(Processed)

        verify(exactly = 1) {
            persistenceContext.saveSubmission(submission)
            filesHandler.processFiles(submission, filesSource)
            submissionProcessor.process(submission, persistenceContext)
        }
    }
}
