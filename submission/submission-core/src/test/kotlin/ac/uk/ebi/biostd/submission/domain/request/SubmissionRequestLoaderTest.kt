package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtResponse
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.common.TEST_CONCURRENCY
import ebi.ac.uk.base.Either.Companion.left
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.INDEXED
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@Disabled
@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestLoaderTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesService: SubmissionRequestFilesPersistenceService,
) {
    private val mockNow = OffsetDateTime.of(2022, 10, 5, 0, 0, 1, 0, UTC)
    private val fireTempDirPath = tempFolder.createDirectory("fire-temp")
    private val testInstance =
        SubmissionRequestLoader(
            TEST_CONCURRENCY,
            fireTempDirPath,
            eventsPublisherService,
            filesService,
            requestService,
        )

    @BeforeEach
    fun beforeEach() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns mockNow
    }

    @AfterEach
    fun afterEach() {
        unmockkStatic(OffsetDateTime::class)
    }

    @Test
    fun `load request`(
        @MockK indexedRequest: SubmissionRequest,
    ) = runTest {
        val filSlot = slot<SubmissionRequestFile>()
        val file = tempFolder.createFile("dummy.txt")
        val nfsFile = NfsFile("dummy.txt", "Files/dummy.txt", file, file.absolutePath, "NOT_CALCULATED", -1)
        val sub = basicExtSubmission.copy(section = ExtSection(type = "Study", files = listOf(left(nfsFile))))
        val indexedFile =
            SubmissionRequestFile(sub.accNo, sub.version, 1, "dummy.txt", nfsFile, RequestFileStatus.INDEXED)

        every { indexedRequest.submission } returns sub
        every { indexedRequest.currentIndex } returns 3
        every { indexedRequest.withNewStatus(RequestStatus.LOADED) } returns indexedRequest
        every { eventsPublisherService.requestLoaded(sub.accNo, sub.version) } answers { nothing }
        every {
            filesService.getSubmissionRequestFiles(
                sub.accNo,
                sub.version,
                RequestFileStatus.INDEXED,
            )
        } returns flowOf(indexedFile)
        coEvery { requestService.updateRqtFile(capture(filSlot)) } answers { nothing }
        coEvery {
            requestService.onRequest(sub.accNo, sub.version, INDEXED, PROCESS_ID, capture(rqtSlot))
        } coAnswers { rqtSlot.captured.invoke(indexedRequest) }

        testInstance.loadRequest(sub.accNo, sub.version, PROCESS_ID)

        val requestFile = filSlot.captured
        assertThat(requestFile.status).isEqualTo(LOADED)
        assertThat(requestFile.file.md5).isEqualTo(file.md5())
        assertThat(requestFile.file.size).isEqualTo(file.size())

        coVerify(exactly = 1) {
            eventsPublisherService.requestLoaded(sub.accNo, sub.version)
        }
    }

    private companion object {
        const val PROCESS_ID = "biostudies-prod"
        val rqtSlot = slot<suspend (SubmissionRequest) -> RqtResponse>()
    }
}
