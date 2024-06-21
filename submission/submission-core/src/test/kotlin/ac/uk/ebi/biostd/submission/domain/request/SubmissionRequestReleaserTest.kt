package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.REUSED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.UNRELEASED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.common.TEST_CONCURRENCY
import ac.uk.ebi.biostd.submission.exceptions.UnreleasedSubmissionException
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.model.RequestStatus.CHECK_RELEASED
import ebi.ac.uk.model.RequestStatus.FILES_COPIED
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestReleaserTest(
    @MockK private val storageService: FileStorageService,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance =
        SubmissionRequestReleaser(
            TEST_CONCURRENCY,
            storageService,
            ExtSerializationService(),
            eventsPublisherService,
            queryService,
            requestService,
            filesService,
        )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `check released when release`(
        @MockK submission: ExtSubmission,
        @MockK rqt: SubmissionRequest,
        @MockK nfsFile: NfsFile,
        @MockK releasedFile: FireFile,
        @MockK fireFile: FireFile,
    ) = runTest {
        val relPath = "sub-relpath"
        val secretKey = "secret-key"
        val mode = StorageMode.FIRE

        val nfsRqtFile = SubmissionRequestFile(ACC_NO, VERSION, 1, "test1.txt", nfsFile, COPIED)
        val fireRqtFile = SubmissionRequestFile(ACC_NO, VERSION, 2, "test2.txt", fireFile, COPIED)

        every { rqt.submission } returns submission
        every { rqt.currentIndex } returns 1
        every { submission.accNo } returns ACC_NO
        every { submission.version } returns VERSION
        every { submission.released } returns true
        every { submission.relPath } returns relPath
        every { submission.secretKey } returns secretKey
        every { submission.storageMode } returns mode
        every { eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION) } answers { nothing }
        every { filesService.getSubmissionRequestFiles(ACC_NO, VERSION, COPIED) } returns
            flowOf(
                nfsRqtFile,
                fireRqtFile,
            )
        coEvery { storageService.releaseSubmissionFile(submission, fireFile) } returns fireFile
        coEvery { storageService.releaseSubmissionFile(submission, nfsFile) } returns releasedFile
        every { rqt.withNewStatus(CHECK_RELEASED) } returns rqt

        coEvery {
            requestService.updateRqtFile(
                nfsRqtFile.copy(
                    file = releasedFile,
                    status = RELEASED,
                ),
            )
        } answers { nothing }
        coEvery { requestService.updateRqtFile(fireRqtFile.copy(status = RELEASED)) } answers { nothing }

        coEvery {
            requestService.onRequest(ACC_NO, VERSION, FILES_COPIED, PROCESS_ID, capture(rqtSlot))
        } coAnswers {
            rqtSlot.captured.invoke(rqt)
        }

        testInstance.checkReleased(ACC_NO, VERSION, PROCESS_ID)

        coVerify(exactly = 1) {
            storageService.releaseSubmissionFile(submission, nfsFile)
            eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION)
        }
    }

    @Test
    fun `check released when not released and no previous version`(
        @MockK rqt: SubmissionRequest,
        @MockK submission: ExtSubmission,
    ) = runTest {
        every { rqt.submission } returns submission
        every { submission.released } returns false
        every { rqt.withNewStatus(CHECK_RELEASED) } returns rqt
        every { eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION) } answers { nothing }

        coEvery { queryService.findCoreInfo(ACC_NO) } returns null
        coEvery {
            requestService.onRequest(ACC_NO, VERSION, FILES_COPIED, PROCESS_ID, capture(rqtSlot))
        } coAnswers {
            rqtSlot.captured.invoke(rqt)
        }

        testInstance.checkReleased(ACC_NO, VERSION, PROCESS_ID)

        verify {
            storageService wasNot called
            persistenceService wasNot called
        }
        coVerify(exactly = 1) {
            eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION)
        }
    }

    @Test
    fun `check released when not released and previous version was private`(
        @MockK rqt: SubmissionRequest,
        @MockK current: ExtSubmission,
        @MockK submission: ExtSubmission,
    ) = runTest {
        every { rqt.submission } returns submission
        every { submission.released } returns false
        every { current.released } returns false
        every { rqt.withNewStatus(CHECK_RELEASED) } returns rqt
        every { eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION) } answers { nothing }

        coEvery { queryService.findCoreInfo(ACC_NO) } returns current
        coEvery {
            requestService.onRequest(ACC_NO, VERSION, FILES_COPIED, PROCESS_ID, capture(rqtSlot))
        } coAnswers {
            rqtSlot.captured.invoke(rqt)
        }

        testInstance.checkReleased(ACC_NO, VERSION, PROCESS_ID)

        verify {
            storageService wasNot called
            persistenceService wasNot called
        }
        coVerify(exactly = 1) {
            eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION)
        }
    }

    @Test
    fun `check released when not released and previous version was public`(
        @MockK current: ExtSubmission,
        @MockK submission: ExtSubmission,
        @MockK rqt: SubmissionRequest,
        @MockK nfsFile: NfsFile,
        @MockK fireFile: FireFile,
        @MockK suppressedNfsFile: NfsFile,
        @MockK suppressedFireFile: FireFile,
    ) = runTest {
        val relPath = "sub-relpath"
        val secretKey = "secret-key"
        val mode = StorageMode.FIRE

        val nfsRqtFile = SubmissionRequestFile(ACC_NO, VERSION, 1, "test1.txt", nfsFile, REUSED)
        val fireRqtFile = SubmissionRequestFile(ACC_NO, VERSION, 2, "test2.txt", fireFile, REUSED)

        every { current.released } returns true
        every { fireFile.published } returns true
        every { rqt.submission } returns submission
        every { rqt.currentIndex } returns 1
        every { submission.accNo } returns ACC_NO
        every { submission.version } returns VERSION
        every { submission.released } returns false
        every { submission.relPath } returns relPath
        every { submission.secretKey } returns secretKey
        every { submission.storageMode } returns mode
        every { eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION) } answers { nothing }
        every { filesService.getSubmissionRequestFiles(ACC_NO, VERSION, REUSED) } returns
            flowOf(
                nfsRqtFile,
                fireRqtFile,
            )
        coEvery { queryService.findCoreInfo(ACC_NO) } returns current
        coEvery { storageService.suppressSubmissionFile(submission, nfsFile) } returns suppressedNfsFile
        coEvery { storageService.suppressSubmissionFile(submission, fireFile) } returns suppressedFireFile
        every { rqt.withNewStatus(CHECK_RELEASED) } returns rqt

        coEvery {
            requestService.updateRqtFile(
                nfsRqtFile.copy(
                    file = suppressedNfsFile,
                    status = UNRELEASED,
                ),
            )
        } answers { nothing }

        coEvery {
            requestService.updateRqtFile(
                fireRqtFile.copy(
                    file = suppressedFireFile,
                    status = UNRELEASED,
                ),
            )
        } answers { nothing }

        coEvery {
            requestService.onRequest(ACC_NO, VERSION, FILES_COPIED, PROCESS_ID, capture(rqtSlot))
        } coAnswers {
            rqtSlot.captured.invoke(rqt)
        }

        testInstance.checkReleased(ACC_NO, VERSION, PROCESS_ID)

        coVerify(exactly = 1) {
            storageService.suppressSubmissionFile(submission, nfsFile)
            storageService.suppressSubmissionFile(submission, fireFile)
            eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION)
        }
    }

    @Test
    fun `generate ftp links for private submission`(
        @MockK submission: ExtSubmission,
    ) = runTest {
        every { submission.released } returns false
        coEvery { queryService.getExtByAccNo(ACC_NO, includeFileListFiles = true) } returns submission

        val exception = assertThrows<UnreleasedSubmissionException> { testInstance.generateFtpLinks(ACC_NO) }

        assertThat(exception.message).isEqualTo("Can't generate FTP links for a private submission")
        coVerify {
            storageService wasNot called
        }
    }

    private companion object {
        const val ACC_NO = "S-TEST123"
        const val VERSION = 1
        const val PROCESS_ID = "biostudies-prod"
        private val rqtSlot = slot<suspend (SubmissionRequest) -> RqtUpdate>()
    }
}
