package ac.uk.ebi.biostd.submission.processing

import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.constants.ProcessingStatus
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
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class SubmissionSubmitterTest(
    @MockK private val filesSource: FilesSource,
    @MockK private val filesHandler: FilesHandler,
    @MockK private val timesService: TimesService,
    @MockK private val accNoService: AccNoService,
    @MockK private val persistenceContext: PersistenceContext,
    @MockK private val submissionRequest: SubmissionRequest,
    @MockK private val user: SecurityUser
) {
    private val submission = createBasicExtendedSubmission()
    private val savedSubmission = slot<ExtendedSubmission>()

    private val testAccNo = AccNumber("ABC", 456)
    private val testTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC)
    private val testInstance = SubmissionSubmitter(filesHandler, persistenceContext, timesService, accNoService)

    @BeforeEach
    fun beforeEach() {
        every { submissionRequest.files } answers { filesSource }
        every { submissionRequest.submission } answers { submission.asSubmission() }
        every { submissionRequest.user } answers { user }
        every { submissionRequest.method } answers { null }

        every { accNoService.getAccNo(submission) } returns testAccNo
        every { accNoService.getRelPath(testAccNo) } returns "ABC/ABCxxx456/ABC456"

        every { persistenceContext.isNew("ABC456") } returns false
        every { persistenceContext.getSecret("ABC456") } returns "a-secret-key"
        every { persistenceContext.getParentAccessTags(submission) } returns emptyList()
        every { persistenceContext.deleteSubmissionDrafts(submission) } answers { nothing }
        every { persistenceContext.saveSubmission(capture(savedSubmission)) } answers { submission }

        every { timesService.getTimes(submission) } returns Times(testTime, testTime, testTime)
        every { filesHandler.processFiles(submission, filesSource) } answers { nothing }
        every { user.asUser() } answers { submission.user }
    }

    @Test
    fun submit() {
        testInstance.submit(submissionRequest, persistenceContext)

        assertThat(savedSubmission.captured.processingStatus).isEqualTo(ProcessingStatus.PROCESSED)
        verify(exactly = 1) {
            timesService.getTimes(submission)
            accNoService.getAccNo(submission)
            accNoService.getRelPath(testAccNo)
            persistenceContext.saveSubmission(submission)
            persistenceContext.deleteSubmissionDrafts(submission)
            persistenceContext.getParentAccessTags(submission)
            filesHandler.processFiles(submission, filesSource)
        }
    }
}
