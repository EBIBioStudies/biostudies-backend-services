package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestLoaderTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    private val mockNow = OffsetDateTime.of(2022, 10, 5, 0, 0, 1, 0, UTC)
    private val testTime = OffsetDateTime.of(2022, 10, 4, 0, 0, 1, 0, UTC)
    private val testInstance = SubmissionRequestLoader(filesRequestService, requestService)

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
    fun `load request`() {
        val loadedRequestSlot = slot<SubmissionRequest>()
        val requestFileSlot = slot<SubmissionRequestFile>()
        val file = tempFolder.createFile("dummy.txt")
        val nfsFile = NfsFile("dummy.txt", "Files/dummy.txt", file, file.absolutePath, "NOT_CALCULATED", -1)
        val sub = basicExtSubmission.copy(section = ExtSection(type = "Study", files = listOf(left(nfsFile))))
        val indexedRequestFile = SubmissionRequestFile(sub.accNo, sub.version, 1, "dummy.txt", nfsFile)
        val indexedRequest = SubmissionRequest(sub, "TMP_123", "user@test.org", INDEXED, 1, 0, testTime)

        every { requestService.getIndexedRequest(sub.accNo, sub.version) } returns indexedRequest
        every { requestService.updateRequestTotalFiles(sub.accNo, sub.version, 1) } answers { nothing }
        every { requestService.saveSubmissionRequest(capture(loadedRequestSlot)) } returns (sub.accNo to sub.version)
        every {
            filesRequestService.getSubmissionRequestFiles(sub.accNo, sub.version, 0)
        } returns listOf(indexedRequestFile).asSequence()
        every {
            requestService.updateRequestFile(capture(requestFileSlot))
        } answers { nothing }

        testInstance.loadRequest(sub.accNo, sub.version)

        val requestFile = requestFileSlot.captured
        assertThat(requestFile.file.md5).isEqualTo(file.md5())
        assertThat(requestFile.file.size).isEqualTo(file.size())

        val loadedRequest = loadedRequestSlot.captured
        assertThat(loadedRequest.submission).isEqualTo(sub)
        assertThat(loadedRequest.draftKey).isEqualTo("TMP_123")
        assertThat(loadedRequest.notifyTo).isEqualTo("user@test.org")
        assertThat(loadedRequest.status).isEqualTo(LOADED)
        assertThat(loadedRequest.totalFiles).isEqualTo(1)
        assertThat(loadedRequest.currentIndex).isEqualTo(0)
        assertThat(loadedRequest.modificationTime).isEqualTo(mockNow)

        verify(exactly = 1) {
            requestService.saveSubmissionRequest(loadedRequest)
            requestService.updateRequestFile(requestFile)
        }
    }
}
