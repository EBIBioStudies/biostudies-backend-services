package ac.uk.ebi.biostd.submission.domain.extended

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestFinalizer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestSaver
import ac.uk.ebi.biostd.submission.domain.submitter.LocalExtSubmissionSubmitter
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class LocalExtSubmissionSubmitterTest(
    @MockK private val properties: ApplicationProperties,
    @MockK private val sub: ExtSubmission,
    @MockK private val pageTabService: PageTabService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val requestIndexer: SubmissionRequestIndexer,
    @MockK private val requestLoader: SubmissionRequestLoader,
    @MockK private val requestProcessor: SubmissionRequestProcessor,
    @MockK private val requestReleaser: SubmissionRequestReleaser,
    @MockK private val requestCleaner: SubmissionRequestCleaner,
    @MockK private val requestSaver: SubmissionRequestSaver,
    @MockK private val requestFinalizer: SubmissionRequestFinalizer,
) {
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 1, 2, 3, 4, UTC)
    private val testInstance = LocalExtSubmissionSubmitter(
        properties,
        pageTabService,
        requestService,
        persistenceService,
        requestIndexer,
        requestLoader,
        requestProcessor,
        requestReleaser,
        requestCleaner,
        requestSaver,
        requestFinalizer,
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockkStatic(OffsetDateTime::class)
        every { properties.processId } returns INSTANCE_ID
        every { OffsetDateTime.now() } returns mockNow
    }

    @Nested
    inner class CreateRequest {
        @Test
        fun `create request`() = runTest {
            val submission = basicExtSubmission
            val submissionRequestSlot = slot<SubmissionRequest>()

            coEvery { persistenceService.getNextVersion("S-TEST123") } returns 2
            coEvery { pageTabService.generatePageTab(submission) } returns submission
            coEvery { requestService.createRequest(capture(submissionRequestSlot)) } returns ("S-TEST123" to 2)

            testInstance.createRequest(ExtSubmitRequest(submission, "user@test.org", "TMP_123"))

            val request = submissionRequestSlot.captured
            coVerify(exactly = 1) {
                pageTabService.generatePageTab(submission)
                requestService.createRequest(request)
            }
            assertThat(request.submission).isEqualTo(submission.copy(version = 2))
            assertThat(request.draftKey).isEqualTo("TMP_123")
            assertThat(request.notifyTo).isEqualTo("user@test.org")
            assertThat(request.status).isEqualTo(REQUESTED)
            assertThat(request.totalFiles).isEqualTo(0)
            assertThat(request.currentIndex).isEqualTo(0)
            assertThat(request.modificationTime).isEqualTo(mockNow)
        }
    }

    @Nested
    inner class RequestStages {
        @Test
        fun `index request`() = runTest {
            coEvery { requestIndexer.indexRequest(ACC_NO, VERSION, INSTANCE_ID) } answers { nothing }

            testInstance.indexRequest(ACC_NO, VERSION)

            coVerify(exactly = 1) { requestIndexer.indexRequest(ACC_NO, VERSION, INSTANCE_ID) }
        }

        @Test
        fun `load request`() = runTest {
            coEvery { requestLoader.loadRequest(ACC_NO, VERSION, INSTANCE_ID) } answers { nothing }

            testInstance.loadRequest(ACC_NO, VERSION)

            coVerify(exactly = 1) { requestLoader.loadRequest(ACC_NO, VERSION, INSTANCE_ID) }
        }

        @Test
        fun `clean request`() = runTest {
            coEvery { requestCleaner.cleanCurrentVersion(ACC_NO, VERSION, INSTANCE_ID) } answers { nothing }

            testInstance.cleanRequest(ACC_NO, VERSION)

            coVerify(exactly = 1) { requestCleaner.cleanCurrentVersion(ACC_NO, VERSION, INSTANCE_ID) }
        }

        @Test
        fun `process request`() = runTest {
            coEvery { requestProcessor.processRequest(ACC_NO, VERSION, INSTANCE_ID) } answers { nothing }

            testInstance.processRequest(ACC_NO, VERSION)

            coVerify(exactly = 1) { requestProcessor.processRequest(ACC_NO, VERSION, INSTANCE_ID) }
        }

        @Test
        fun `check released`() = runTest {
            coEvery { requestReleaser.checkReleased(ACC_NO, VERSION, INSTANCE_ID) } answers { nothing }

            testInstance.checkReleased(ACC_NO, VERSION)

            coVerify(exactly = 1) { requestReleaser.checkReleased(ACC_NO, VERSION, INSTANCE_ID) }
        }

        @Test
        fun `save request`() = runTest {
            coEvery { requestSaver.saveRequest(ACC_NO, VERSION, INSTANCE_ID) } returns sub

            testInstance.saveRequest(ACC_NO, VERSION)

            coVerify(exactly = 1) { requestSaver.saveRequest(ACC_NO, VERSION, INSTANCE_ID) }
        }

        @Test
        fun `finalize request`() = runTest {
            coEvery { requestFinalizer.finalizeRequest(ACC_NO, VERSION, INSTANCE_ID) } returns sub

            testInstance.finalizeRequest(ACC_NO, VERSION)

            coVerify(exactly = 1) { requestFinalizer.finalizeRequest(ACC_NO, VERSION, INSTANCE_ID) }
        }

        @Test
        fun `release request`() = runTest {
            coEvery { requestReleaser.releaseSubmission(ACC_NO) } answers { nothing }

            testInstance.release(ACC_NO)

            coVerify(exactly = 1) { requestReleaser.releaseSubmission(ACC_NO) }
        }
    }

    private companion object {
        const val ACC_NO = "S-BSST1"
        const val INSTANCE_ID = "biostudies-prod"
        const val VERSION = 1
    }
}
