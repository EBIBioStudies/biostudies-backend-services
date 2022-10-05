package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.api.FireFilePersistenceConfig
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
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.FileProcessingService

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestProcessorTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val storageService: FileStorageService,
    @MockK private val fileService: FileProcessingService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val persistenceService: SubmissionPersistenceService,
) {
    private val testInstance = SubmissionRequestProcessor(storageService, fileService, queryService, persistenceService)

    @Test
    fun `process request`() {
        val submission = basicExtSubmission
        val processedRequestSlot = slot<SubmissionRequest>()
        val cleanedRequest = SubmissionRequest(submission, "TMP_123", CLEANED)
        val fireFile = FireFile("test.txt", "Files/test.txt", "abc1", "md5", 1, FILE, emptyList())
        val nfsFile = createNfsFile("dummy.txt", "Files/dummy.txt", tempFolder.createFile("dummy.txt"))
        val config = FireFilePersistenceConfig(submission.accNo, submission.version, submission.relPath)
        val processed = submission.copy(section = submission.section.copy(files = listOf(Either.left(fireFile))))

        every { persistenceService.saveSubmission(processed) } returns processed
        every { storageService.preProcessSubmissionFiles(submission) } returns config
        every { storageService.postProcessSubmissionFiles(config) } answers { nothing }
        every { storageService.persistSubmissionFile(nfsFile, config) } returns fireFile
        every { queryService.getCleanedRequest(submission.accNo, 1) } returns cleanedRequest
        every { persistenceService.expirePreviousVersions(submission.accNo) } answers { nothing }
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
        verify(exactly = 1) {
            storageService.preProcessSubmissionFiles(submission)
            storageService.persistSubmissionFile(nfsFile, config)
            storageService.postProcessSubmissionFiles(config)
            persistenceService.expirePreviousVersions(submission.accNo)
            persistenceService.saveSubmission(processed)
            persistenceService.saveSubmissionRequest(processedRequest)
        }
    }
}
