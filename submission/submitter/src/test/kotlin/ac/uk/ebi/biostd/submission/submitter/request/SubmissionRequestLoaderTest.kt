package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtFile
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
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestLoaderTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val fileProcessingService: FileProcessingService,
    @MockK private val pageTabService: PageTabService,
) {
    private val mockNow = OffsetDateTime.of(2022, 10, 5, 0, 0, 1, 0, ZoneOffset.UTC)
    private val testInstance =
        SubmissionRequestLoader(
            queryService,
            persistenceService,
            fileProcessingService,
            pageTabService,
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
        @MockK pendingRequest: SubmissionRequest
    ) {
        val loadedRequestSlot = slot<SubmissionRequest>()
        val file = tempFolder.createFile("dummy.txt")
        var nfsFile = NfsFile("dummy.txt", "Files/dummy.txt", file, file.absolutePath, "NOT_CALCULATED", -1)
        val sub = basicExtSubmission.copy(section = ExtSection(type = "Study", files = listOf(left(nfsFile))))

        every { pendingRequest.submission } returns sub
        every { pendingRequest.draftKey } returns "TMP_123"
        every { pageTabService.generatePageTab(sub) } returns sub
        every { queryService.getPendingRequest(sub.accNo, sub.version) } returns pendingRequest
        every { persistenceService.updateRequestTotalFiles(sub.accNo, sub.version, 1) } answers { nothing }
        every { persistenceService.updateRequestIndex(sub.accNo, sub.version, 1) } answers { nothing }
        every {
            persistenceService.saveSubmissionRequest(capture(loadedRequestSlot))
        } returns (sub.accNo to sub.version)
        every { fileProcessingService.processFiles(sub, any()) } answers {
            val function: (file: ExtFile, index: Int) -> ExtFile = secondArg()
            nfsFile = function(nfsFile, 1) as NfsFile
            sub
        }

        val loaded = testInstance.loadRequest(sub.accNo, sub.version)
        assertThat(loaded).isEqualTo(sub)
        assertThat(nfsFile.md5).isEqualTo(file.md5())
        assertThat(nfsFile.size).isEqualTo(file.size())

        val loadedRequest = loadedRequestSlot.captured
        assertThat(loadedRequest.submission).isEqualTo(sub)
        assertThat(loadedRequest.draftKey).isEqualTo("TMP_123")
        assertThat(loadedRequest.status).isEqualTo(LOADED)
        assertThat(loadedRequest.totalFiles).isEqualTo(1)
        assertThat(loadedRequest.currentIndex).isEqualTo(0)
        assertThat(loadedRequest.modificationTime).isEqualTo(mockNow)

        verify(exactly = 1) {
            pageTabService.generatePageTab(sub)
            persistenceService.saveSubmissionRequest(loadedRequest)
            persistenceService.updateRequestIndex(sub.accNo, sub.version, 1)
            persistenceService.updateRequestTotalFiles(sub.accNo, sub.version, 1)
        }
    }
}
