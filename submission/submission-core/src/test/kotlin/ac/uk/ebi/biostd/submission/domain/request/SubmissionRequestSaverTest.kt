package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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
        val accNo = "ABC-123"
        val version = 1
        val changeId = "changeId"
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
            requestService.getSubmissionRequest(
                accNo,
                version,
                CHECK_RELEASED,
                INSTANCE_ID
            )
        } answers { (changeId to request) }
        coEvery { requestService.saveRequest(request) } answers { ACC_NO to version }

        every { request.withNewStatus(PERSISTED, changeId) } returns request
        every { request.submission } answers { submission }
        every { request.notifyTo } answers { notifyTo }
        every { requestFile.file } returns updatedSubFile

        coEvery { filesService.getSubmissionRequestFile(accNo, version, filePath) } returns requestFile

        var newFile: ExtFile? = null
        coEvery { processingService.processFiles(submission, any()) } coAnswers {
            newFile = secondArg<suspend (file: ExtFile) -> ExtFile>()(subFile)
            submission
        }

        val response = testInstance.saveRequest(accNo, version, INSTANCE_ID)

        assertThat(response).isEqualTo(submission)
        assertThat(newFile).isEqualTo(updatedSubFile)
        coVerify(exactly = 1) {
            eventsPublisherService.submissionSubmitted(accNo, notifyTo)
            eventsPublisherService.submissionPersisted(accNo, version)
            persistenceService.expirePreviousVersions(accNo)
            persistenceService.saveSubmission(submission)
            requestService.saveRequest(request)
        }
    }

    private companion object {
        const val INSTANCE_ID = "biostudies-prod"
    }
}
