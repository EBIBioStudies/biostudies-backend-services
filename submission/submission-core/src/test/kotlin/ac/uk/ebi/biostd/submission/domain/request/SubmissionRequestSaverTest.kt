package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtResponse
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.model.RequestStatus.CHECK_RELEASED
import ebi.ac.uk.model.RequestStatus.PERSISTED
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import java.io.File

@ExtendWith(MockKExtension::class)
internal class SubmissionRequestSaverTest(
    @MockK val requestService: SubmissionRequestPersistenceService,
    @MockK val processingService: FileProcessingService,
    @MockK val persistenceService: SubmissionPersistenceService,
    @MockK val filesService: SubmissionRequestFilesPersistenceService,
    @MockK val eventsPublisherService: EventsPublisherService,
) {
    private val testInstance =
        SubmissionRequestSaver(
            requestService,
            processingService,
            persistenceService,
            filesService,
            eventsPublisherService,
        )

    @Test
    fun saveRequest(
        @MockK request: SubmissionRequest,
        @MockK submission: ExtSubmission,
        @MockK subFile: ExtFile,
        @MockK requestFile: SubmissionRequestFile,
        @MockK updatedFile: File,
    ) = runTest {
        val filePath = "the-file-path"
        val notifyTo = "user@ebi.ac.uk"
        val updatedSubFile = NfsFile("file.txt", "Files/file.txt", updatedFile, "file", "md5", 1, type = FILE)

        every { subFile.attributes } returns emptyList()
        every { eventsPublisherService.submissionPersisted(ACC_NO, VERSION) } answers { nothing }
        every { eventsPublisherService.submissionSubmitted(ACC_NO, notifyTo) } answers { nothing }
        coEvery { persistenceService.expirePreviousVersions(ACC_NO) } answers { nothing }
        coEvery { persistenceService.saveSubmission(submission) } answers { submission }

        every { submission.accNo } answers { ACC_NO }
        every { submission.version } answers { VERSION }
        every { subFile.filePath } returns filePath

        coEvery {
            requestService.onRequest(ACC_NO, VERSION, CHECK_RELEASED, PROCESS_ID, capture(rqtSlot))
        } coAnswers { rqtSlot.captured.invoke(request) }

        every { request.withNewStatus(PERSISTED) } returns request
        every { request.submission } answers { submission }
        every { request.notifyTo } answers { notifyTo }
        every { requestFile.file } returns updatedSubFile

        coEvery { filesService.getSubmissionRequestFile(ACC_NO, VERSION, filePath) } returns requestFile

        var newFile: ExtFile? = null
        coEvery { processingService.processFiles(submission, any()) } coAnswers {
            newFile = secondArg<suspend (file: ExtFile) -> ExtFile>()(subFile)
            submission
        }

        val response = testInstance.saveRequest(ACC_NO, VERSION, PROCESS_ID)

        assertThat(response).isEqualTo(submission)
        assertThat(newFile).isEqualTo(updatedSubFile)
        coVerify(exactly = 1) {
            eventsPublisherService.submissionSubmitted(ACC_NO, notifyTo)
            eventsPublisherService.submissionPersisted(ACC_NO, VERSION)
            persistenceService.expirePreviousVersions(ACC_NO)
            persistenceService.saveSubmission(submission)
        }
    }

    private companion object {
        val ACC_NO = "ABC-123"
        val VERSION = 1
        const val PROCESS_ID = "biostudies-prod"
        val rqtSlot = slot<suspend (SubmissionRequest) -> RqtResponse>()
    }
}
