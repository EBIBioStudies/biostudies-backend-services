package ac.uk.ebi.biostd.submission.domain.extended

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PAGE_TAB_GENERATED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestFinalizer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestPageTabGenerator
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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class LocalExtSubmissionSubmitterTest(
    @MockK private val properties: ApplicationProperties,
    @MockK private val sub: ExtSubmission,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val requestIndexer: SubmissionRequestIndexer,
    @MockK private val requestLoader: SubmissionRequestLoader,
    @MockK private val requestPageTabGenerator: SubmissionRequestPageTabGenerator,
    @MockK private val requestProcessor: SubmissionRequestProcessor,
    @MockK private val requestReleaser: SubmissionRequestReleaser,
    @MockK private val requestCleaner: SubmissionRequestCleaner,
    @MockK private val requestSaver: SubmissionRequestSaver,
    @MockK private val requestFinalizer: SubmissionRequestFinalizer,
) {
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 1, 2, 3, 4, UTC)
    private val testInstance = LocalExtSubmissionSubmitter(
        properties,
        requestService,
        persistenceService,
        requestIndexer,
        requestLoader,
        requestPageTabGenerator,
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
        every { properties.processId } returns instanceId
        every { OffsetDateTime.now() } returns mockNow
    }

    @Nested
    inner class CreateRequest {
        @Test
        fun `create request`() = runTest {
            val submission = basicExtSubmission
            val submissionRequestSlot = slot<SubmissionRequest>()

            coEvery { persistenceService.getNextVersion("S-TEST123") } returns 2
            coEvery { requestService.createRequest(capture(submissionRequestSlot)) } returns ("S-TEST123" to 2)

            testInstance.createRequest(ExtSubmitRequest(submission, "user@test.org", "TMP_123"))

            val request = submissionRequestSlot.captured
            coVerify(exactly = 1) {
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
    inner class HandleRequest {
        @Test
        fun `when requested`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns REQUESTED
            coEvery { requestIndexer.indexRequest("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestLoader.loadRequest("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestPageTabGenerator.generatePageTab("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestProcessor.processRequest("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestReleaser.checkReleased("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestCleaner.cleanCurrentVersion("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestSaver.saveRequest("accNo", 1, instanceId) } answers { sub }
            coEvery { requestFinalizer.finalizeRequest("accNo", 1, instanceId) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestIndexer.indexRequest("accNo", 1, instanceId)
                requestLoader.loadRequest("accNo", 1, instanceId)
                requestPageTabGenerator.generatePageTab("accNo", 1, instanceId)
                requestCleaner.cleanCurrentVersion("accNo", 1, instanceId)
                requestProcessor.processRequest("accNo", 1, instanceId)
                requestReleaser.checkReleased("accNo", 1, instanceId)
                requestSaver.saveRequest("accNo", 1, instanceId)
                requestFinalizer.finalizeRequest("accNo", 1, instanceId)
            }
        }

        @Test
        fun `when loaded`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns LOADED
            coEvery { requestPageTabGenerator.generatePageTab("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestProcessor.processRequest("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestReleaser.checkReleased("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestCleaner.cleanCurrentVersion("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestSaver.saveRequest("accNo", 1, instanceId) } answers { sub }
            coEvery { requestFinalizer.finalizeRequest("accNo", 1, instanceId) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestPageTabGenerator.generatePageTab("accNo", 1, instanceId)
                requestCleaner.cleanCurrentVersion("accNo", 1, instanceId)
                requestProcessor.processRequest("accNo", 1, instanceId)
                requestReleaser.checkReleased("accNo", 1, instanceId)
                requestSaver.saveRequest("accNo", 1, instanceId)
                requestFinalizer.finalizeRequest("accNo", 1, instanceId)
            }
            coVerify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1, instanceId)
                requestLoader.loadRequest("accNo", 1, instanceId)
            }
        }

        @Test
        fun `when pagetab generated`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns PAGE_TAB_GENERATED
            coEvery { requestPageTabGenerator.generatePageTab("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestProcessor.processRequest("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestReleaser.checkReleased("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestCleaner.cleanCurrentVersion("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestSaver.saveRequest("accNo", 1, instanceId) } answers { sub }
            coEvery { requestFinalizer.finalizeRequest("accNo", 1, instanceId) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1, instanceId)
                requestProcessor.processRequest("accNo", 1, instanceId)
                requestReleaser.checkReleased("accNo", 1, instanceId)
                requestSaver.saveRequest("accNo", 1, instanceId)
                requestFinalizer.finalizeRequest("accNo", 1, instanceId)
            }
            coVerify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1, instanceId)
                requestLoader.loadRequest("accNo", 1, instanceId)
                requestPageTabGenerator.generatePageTab("accNo", 1, instanceId)
            }
        }

        @Test
        fun `when cleaned`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns CLEANED
            coEvery { requestProcessor.processRequest("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestReleaser.checkReleased("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestSaver.saveRequest("accNo", 1, instanceId) } answers { sub }
            coEvery { requestFinalizer.finalizeRequest("accNo", 1, instanceId) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestProcessor.processRequest("accNo", 1, instanceId)
                requestReleaser.checkReleased("accNo", 1, instanceId)
                requestSaver.saveRequest("accNo", 1, instanceId)
                requestFinalizer.finalizeRequest("accNo", 1, instanceId)
            }
            coVerify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1, instanceId)
                requestLoader.loadRequest("accNo", 1, instanceId)
                requestCleaner.cleanCurrentVersion("accNo", 1, instanceId)
            }
        }

        @Test
        fun `when files copied`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns FILES_COPIED
            coEvery { requestReleaser.checkReleased("accNo", 1, instanceId) } answers { nothing }
            coEvery { requestSaver.saveRequest("accNo", 1, instanceId) } answers { sub }
            coEvery { requestFinalizer.finalizeRequest("accNo", 1, instanceId) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestReleaser.checkReleased("accNo", 1, instanceId)
                requestSaver.saveRequest("accNo", 1, instanceId)
                requestFinalizer.finalizeRequest("accNo", 1, instanceId)
            }
            coVerify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1, instanceId)
                requestLoader.loadRequest("accNo", 1, instanceId)
                requestCleaner.cleanCurrentVersion("accNo", 1, instanceId)
                requestProcessor.processRequest("accNo", 1, instanceId)
            }
        }

        @Test
        fun `when checked released`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns CHECK_RELEASED
            coEvery { requestSaver.saveRequest("accNo", 1, instanceId) } returns sub
            coEvery { requestFinalizer.finalizeRequest("accNo", 1, instanceId) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestSaver.saveRequest("accNo", 1, instanceId)
                requestFinalizer.finalizeRequest("accNo", 1, instanceId)
            }
            coVerify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1, instanceId)
                requestLoader.loadRequest("accNo", 1, instanceId)
                requestCleaner.cleanCurrentVersion("accNo", 1, instanceId)
                requestProcessor.processRequest("accNo", 1, instanceId)
                requestReleaser.checkReleased("accNo", 1, instanceId)
            }
        }

        @Test
        fun `when persisted`() = runTest {
            coEvery { requestFinalizer.finalizeRequest("accNo", 1, instanceId) } returns sub
            coEvery { requestService.getRequestStatus("accNo", 1) } returns PERSISTED

            testInstance.handleRequest("accNo", 1)

            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestFinalizer.finalizeRequest("accNo", 1, instanceId)
            }
            coVerify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1, instanceId)
                requestLoader.loadRequest("accNo", 1, instanceId)
                requestCleaner.cleanCurrentVersion("accNo", 1, instanceId)
                requestProcessor.processRequest("accNo", 1, instanceId)
                requestReleaser.checkReleased("accNo", 1, instanceId)
                requestSaver.saveRequest("accNo", 1, instanceId)
            }
        }

        @Test
        fun `when processed`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns PROCESSED

            val exception = assertThrows<IllegalStateException> { testInstance.handleRequest("accNo", 1) }
            assertThat(exception.message).isEqualTo("Request accNo=accNo, version='1' has been already processed")
        }
    }

    private companion object {
        const val instanceId = "biostudies-prod"
    }
}
