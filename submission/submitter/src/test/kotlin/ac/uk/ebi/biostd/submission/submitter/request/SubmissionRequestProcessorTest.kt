package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
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
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val persistenceService: SubmissionPersistenceService,
) {
    private val mockNow = OffsetDateTime.of(2022, 10, 5, 0, 0, 1, 0, UTC)
    private val testTime = OffsetDateTime.of(2022, 10, 5, 0, 0, 0, 0, UTC)
    private val testInstance = SubmissionRequestProcessor(storageService, fileService, queryService, persistenceService)

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
        val submission = basicExtSubmission
        val processedRequestSlot = slot<SubmissionRequest>()
        val cleanedRequest = SubmissionRequest(submission, "TMP_123", CLEANED, modificationTime = testTime)
        val fireFile = FireFile("test.txt", "Files/test.txt", "abc1", "md5", 1, FILE, emptyList())
        val nfsFile = createNfsFile("dummy.txt", "Files/dummy.txt", tempFolder.createFile("dummy.txt"))
        val processed = submission.copy(section = submission.section.copy(files = listOf(Either.left(fireFile))))

        every { persistenceService.saveSubmission(processed) } returns processed
        every { storageService.postProcessSubmissionFiles(submission) } answers { nothing }
        every { storageService.persistSubmissionFile(submission, nfsFile) } returns fireFile
        every { queryService.getCleanedRequest(submission.accNo, 1) } returns cleanedRequest
        every { persistenceService.expirePreviousVersions(submission.accNo) } answers { nothing }
        every { persistenceService.updateRequestIndex(submission.accNo, submission.version, 1) } answers { nothing }
        every {
            persistenceService.saveSubmissionRequest(capture(processedRequestSlot))
        } returns (submission.accNo to submission.version)
        every { fileService.processFiles(submission, any()) } answers {
            val function: (file: ExtFile, index: Int) -> ExtFile = secondArg()
            function(nfsFile, 1)
            processed
        }

        val result = testInstance.processRequest(submission.accNo, submission.version)
        val processedRequest = processedRequestSlot.captured

        assertThat(result).isEqualTo(processed)
        assertThat(processedRequest.status).isEqualTo(FILES_COPIED)
        assertThat(processedRequest.modificationTime).isEqualTo(mockNow)
        verify(exactly = 1) {
            storageService.persistSubmissionFile(submission, nfsFile)
            storageService.postProcessSubmissionFiles(submission)
            persistenceService.expirePreviousVersions(submission.accNo)
            persistenceService.saveSubmission(processed)
            persistenceService.saveSubmissionRequest(processedRequest)
        }
    }
}
