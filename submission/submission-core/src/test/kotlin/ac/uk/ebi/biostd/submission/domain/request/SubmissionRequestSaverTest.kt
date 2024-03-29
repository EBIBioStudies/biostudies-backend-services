package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
internal class SubmissionRequestSaverTest(
    @MockK val requestService: SubmissionRequestPersistenceService,
    @MockK val processingService: FileProcessingService,
    @MockK val persistenceService: SubmissionPersistenceService,
    @MockK val filesService: SubmissionRequestFilesPersistenceService,
    @MockK val eventsPublisherService: EventsPublisherService,
) {
    private val testInstance = SubmissionRequestSaver(
        requestService,
        processingService,
        persistenceService,
        filesService,
        eventsPublisherService
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
        every { eventsPublisherService.submissionPersisted(accNo, version) } answers { nothing }
        every { eventsPublisherService.submissionSubmitted(accNo, notifyTo) } answers { nothing }
        coEvery { persistenceService.expirePreviousVersions(accNo) } answers { nothing }
        coEvery { persistenceService.saveSubmission(submission) } answers { submission }

        every { submission.accNo } answers { accNo }
        every { submission.version } answers { version }
        every { subFile.filePath } returns filePath

        coEvery {
            requestService.onRequest(accNo, version, CHECK_RELEASED, processId, capture(rqtSlot))
        } coAnswers { rqtSlot.captured.invoke(request) }

        every { request.withNewStatus(PERSISTED) } returns request
        every { request.submission } answers { submission }
        every { request.notifyTo } answers { notifyTo }
        every { requestFile.file } returns updatedSubFile

        coEvery { filesService.getSubmissionRequestFile(accNo, version, filePath) } returns requestFile

        var newFile: ExtFile? = null
        coEvery { processingService.processFiles(submission, any()) } coAnswers {
            newFile = secondArg<suspend (file: ExtFile) -> ExtFile>()(subFile)
            submission
        }

        val response = testInstance.saveRequest(accNo, version, processId)

        assertThat(response).isEqualTo(submission)
        assertThat(newFile).isEqualTo(updatedSubFile)
        coVerify(exactly = 1) {
            eventsPublisherService.submissionSubmitted(accNo, notifyTo)
            eventsPublisherService.submissionPersisted(accNo, version)
            persistenceService.expirePreviousVersions(accNo)
            persistenceService.saveSubmission(submission)
        }
    }

    private companion object {
        val accNo = "ABC-123"
        val version = 1
        const val processId = "biostudies-prod"
        val rqtSlot = slot<suspend (SubmissionRequest) -> RqtResponse>()
    }
}
