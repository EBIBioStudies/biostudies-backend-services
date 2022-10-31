package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.createNfsFile
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
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestProcessorTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val storageService: FileStorageService,
    @MockK private val fileService: FileProcessingService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    private val mockNow = OffsetDateTime.of(2022, 10, 5, 0, 0, 1, 0, UTC)
    private val testTime = OffsetDateTime.of(2022, 10, 5, 0, 0, 0, 0, UTC)
    private val testInstance =
        SubmissionRequestProcessor(
            storageService,
            fileService,
            persistenceService,
            requestService,
            filesRequestService,
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
    fun `process request`() {
        val sub = basicExtSubmission
        val requestFileSlot = slot<SubmissionRequestFile>()
        val processedRequestSlot = slot<SubmissionRequest>()
        val cleanedRequest = SubmissionRequest(sub, "TMP_123", CLEANED, 1, 0, modificationTime = testTime)
        val fireFile = FireFile("abc1", null, "test.txt", "Files/test.txt", "md5", 1, FILE, emptyList())
        val nfsFile = createNfsFile("dummy.txt", "Files/dummy.txt", tempFolder.createFile("dummy.txt"))
        val loadedRequestFile = SubmissionRequestFile(sub.accNo, sub.version, 1, "test.txt", nfsFile)
        val processed = sub.copy(section = sub.section.copy(files = listOf(left(fireFile))))

        every { fileService.processFiles(sub, any()) } returns processed
        every { persistenceService.saveSubmission(processed) } returns processed
        every { storageService.postProcessSubmissionFiles(sub) } answers { nothing }
        every { storageService.persistSubmissionFile(sub, nfsFile) } returns fireFile
        every { requestService.getCleanedRequest(sub.accNo, 1) } returns cleanedRequest
        every { persistenceService.expirePreviousVersions(sub.accNo) } answers { nothing }
        every { requestService.updateRequestIndex(sub.accNo, sub.version, 1) } answers { nothing }
        every {
            requestService.saveSubmissionRequest(capture(processedRequestSlot))
        } returns (sub.accNo to sub.version)
        every {
            filesRequestService.getSubmissionRequestFiles(sub.accNo, sub.version, 0)
        } returns sequenceOf(loadedRequestFile)
        every { filesRequestService.saveSubmissionRequestFile(capture(requestFileSlot)) } answers { nothing }

        val result = testInstance.processRequest(sub.accNo, sub.version)
        val processedRequest = processedRequestSlot.captured
        val requestFile = requestFileSlot.captured

        assertThat(result).isEqualTo(processed)
        assertThat(requestFile.file).isEqualTo(fireFile)
        assertThat(processedRequest.status).isEqualTo(FILES_COPIED)
        assertThat(processedRequest.modificationTime).isEqualTo(mockNow)
        verify(exactly = 1) {
            storageService.persistSubmissionFile(sub, nfsFile)
            storageService.postProcessSubmissionFiles(sub)
            persistenceService.expirePreviousVersions(sub.accNo)
            persistenceService.saveSubmission(processed)
            requestService.saveSubmissionRequest(processedRequest)
            requestService.updateRequestIndex(sub.accNo, sub.version, 1)
            filesRequestService.saveSubmissionRequestFile(requestFile)
        }
    }
}
