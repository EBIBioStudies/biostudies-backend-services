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
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.model.RequestStatus.CHECK_RELEASED
import ebi.ac.uk.model.RequestStatus.FILES_COPIED
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class)
class SubmissionRequestReleaserTest(
    @MockK private val nfsFile: NfsFile,
    @MockK private val fireFile: FireFile,
    @MockK private val rqt: SubmissionRequest,
    @MockK private val submission: ExtSubmission,
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

    @BeforeEach
    fun beforeEach() {
        every { fireFile.published } returns false

        every { rqt.submission } returns submission
        every { rqt.currentIndex } returns CURRENT_INDEX
        every { rqt.withNewStatus(CHECK_RELEASED) } returns rqt

        every { submission.accNo } returns ACC_NO
        every { submission.version } returns VERSION
        every { submission.released } returns true
        every { submission.relPath } returns REL_PATH
        every { submission.secretKey } returns SECRET_KEY
        every { submission.storageMode } returns FIRE

        every { eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION) } answers { nothing }

        coEvery {
            requestService.onRequest(ACC_NO, VERSION, FILES_COPIED, PROCESS_ID, capture(rqtSlot))
        } coAnswers {
            rqtSlot.captured.invoke(rqt)
        }
    }

    @Nested
    inner class ReleaseSubmission(
        @MockK private val releasedFile: FireFile,
    ) {
        private val nfsRqtFile = SubmissionRequestFile(ACC_NO, VERSION, 1, "test1.txt", nfsFile, COPIED)
        private val fireRqtFile = SubmissionRequestFile(ACC_NO, VERSION, 2, "test2.txt", fireFile, COPIED)

        @BeforeEach
        fun beforeEach() {
            coEvery { storageService.releaseSubmissionFile(submission, fireFile) } returns fireFile
            coEvery { storageService.releaseSubmissionFile(submission, nfsFile) } returns releasedFile

            every { filesService.getSubmissionRequestFiles(ACC_NO, VERSION, COPIED) } returns
                flowOf(
                    nfsRqtFile,
                    fireRqtFile,
                )

            coEvery { requestService.updateRqtFile(fireRqtFile.copy(status = RELEASED)) } answers { nothing }
            coEvery {
                requestService.updateRqtFile(
                    nfsRqtFile.copy(
                        file = releasedFile,
                        status = RELEASED,
                    ),
                )
            } answers { nothing }
        }

        @Test
        fun `check release for public submission`() =
            runTest {
                testInstance.checkReleased(ACC_NO, VERSION, PROCESS_ID)

                coVerify(exactly = 1) {
                    storageService.releaseSubmissionFile(submission, nfsFile)
                    storageService.releaseSubmissionFile(submission, fireFile)
                    eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION)
                }
            }

        @Test
        fun `check release for public submission with FIRE file already published`() =
            runTest {
                every { fireFile.published } returns true

                testInstance.checkReleased(ACC_NO, VERSION, PROCESS_ID)

                coVerify(exactly = 1) {
                    storageService.releaseSubmissionFile(submission, nfsFile)
                    eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION)
                }
                coVerify(exactly = 0) {
                    storageService.releaseSubmissionFile(submission, fireFile)
                }
            }

        @Test
        fun `check released for private submission`() =
            runTest {
                every { submission.released } returns false

                coEvery { queryService.findCoreInfo(ACC_NO) } returns null

                testInstance.checkReleased(ACC_NO, VERSION, PROCESS_ID)

                verify {
                    storageService wasNot called
                    persistenceService wasNot called
                }
                coVerify(exactly = 1) {
                    eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION)
                }
            }
    }

    @Nested
    inner class UnReleaseSubmission(
        @MockK private val current: ExtSubmission,
        @MockK private val suppressedNfsFile: NfsFile,
        @MockK private val suppressedFireFile: FireFile,
    ) {
        private val nfsRqtFile = SubmissionRequestFile(ACC_NO, VERSION, 1, "test1.txt", nfsFile, REUSED)
        private val fireRqtFile = SubmissionRequestFile(ACC_NO, VERSION, 2, "test2.txt", fireFile, REUSED)

        @BeforeEach
        fun beforeEach() {
            every { current.released } returns false

            coEvery { queryService.findCoreInfo(ACC_NO) } returns current

            every { filesService.getSubmissionRequestFiles(ACC_NO, VERSION, REUSED) } returns
                flowOf(
                    nfsRqtFile,
                    fireRqtFile,
                )

            coEvery { storageService.unReleaseSubmissionFile(submission, nfsFile) } returns suppressedNfsFile
            coEvery { storageService.unReleaseSubmissionFile(submission, fireFile) } returns suppressedFireFile

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
        }

        @Test
        fun `check released for private submission with private previous version`() =
            runTest {
                every { submission.released } returns false

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
        fun `check released for private submission with public previous version`() =
            runTest {
                every { current.released } returns true
                every { fireFile.published } returns true
                every { submission.released } returns false

                testInstance.checkReleased(ACC_NO, VERSION, PROCESS_ID)

                coVerify(exactly = 1) {
                    storageService.unReleaseSubmissionFile(submission, nfsFile)
                    storageService.unReleaseSubmissionFile(submission, fireFile)
                    eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION)
                }
            }

        @Test
        fun `check released for private submission with public previous version and fire file already unreleased`() =
            runTest {
                every { current.released } returns true
                every { fireFile.published } returns false
                every { submission.released } returns false
                coEvery {
                    requestService.updateRqtFile(
                        fireRqtFile.copy(
                            file = fireFile,
                            status = UNRELEASED,
                        ),
                    )
                } answers { nothing }

                testInstance.checkReleased(ACC_NO, VERSION, PROCESS_ID)

                coVerify(exactly = 1) {
                    storageService.unReleaseSubmissionFile(submission, nfsFile)
                    eventsPublisherService.requestCheckedRelease(ACC_NO, VERSION)
                }
                coVerify(exactly = 0) {
                    storageService.unReleaseSubmissionFile(submission, fireFile)
                }
            }
    }

    @Nested
    inner class GenerateFtpLinks {
        @Test
        fun `generate ftp links for private submission`() =
            runTest {
                every { submission.released } returns false
                coEvery { queryService.getExtByAccNo(ACC_NO, includeFileListFiles = true) } returns submission

                val exception = assertThrows<UnreleasedSubmissionException> { testInstance.generateFtpLinks(ACC_NO) }

                assertThat(exception.message).isEqualTo("Can't generate FTP links for a private submission")
                coVerify {
                    storageService wasNot called
                }
            }
    }

    private companion object {
        const val ACC_NO = "S-TEST123"
        const val CURRENT_INDEX = 1
        const val PROCESS_ID = "biostudies-prod"
        const val REL_PATH = "sub-relpath"
        const val SECRET_KEY = "secret-key"
        const val VERSION = 1
        private val rqtSlot = slot<suspend (SubmissionRequest) -> RqtUpdate>()
    }
}
