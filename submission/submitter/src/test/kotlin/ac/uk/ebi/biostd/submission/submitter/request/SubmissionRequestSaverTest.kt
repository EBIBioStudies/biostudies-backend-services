package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.model.constants.ACC_NO
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
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
    ) {
        val accNo = "ABC-123"
        val version = 1
        val filePath = "the-file-path"
        val notifyTo = "user@ebi.ac.uk"
        val updatedSubFile = NfsFile("file.txt", "Files/file.txt", updatedFile, "file", "md5", 1, type = FILE)

        every { subFile.attributes } returns emptyList()
        every { eventsPublisherService.submissionSubmitted(accNo, notifyTo) } answers { nothing }
        every { persistenceService.expirePreviousVersions(accNo) } answers { nothing }
        every { persistenceService.saveSubmission(submission) } answers { submission }

        every { submission.accNo } answers { accNo }
        every { submission.version } answers { version }
        every { subFile.filePath } returns filePath

        every { requestService.getCheckReleased(accNo, version) } answers { request }
        every { requestService.saveSubmissionRequest(request) } answers { ACC_NO to version }

        every { request.withNewStatus(PROCESSED) } returns request
        every { request.submission } answers { submission }
        every { request.notifyTo } answers { notifyTo }
        every { requestFile.file } returns updatedSubFile

        every { filesService.getSubmissionRequestFile(accNo, version, filePath) } returns requestFile

        var newFile: ExtFile? = null
        every { processingService.processFiles(submission, any()) } answers {
            newFile = secondArg<(file: ExtFile) -> ExtFile>()(subFile)
            submission
        }

        val response = testInstance.saveRequest(accNo, version)

        assertThat(response).isEqualTo(submission)
        assertThat(newFile).isEqualTo(updatedSubFile)
        verify(exactly = 1) {
            eventsPublisherService.submissionSubmitted(accNo, notifyTo)
            persistenceService.expirePreviousVersions(accNo)
            persistenceService.saveSubmission(submission)
            requestService.saveSubmissionRequest(request)
        }
    }
}
