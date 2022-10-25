package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.NfsFile
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
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import kotlin.streams.asStream

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestIndexerTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance = SubmissionRequestIndexer(ExtSerializationService(), requestService, filesRequestService)

    @Test
    fun `index request`(
        @MockK pendingRequest: SubmissionRequest,
        @MockK mockRequestFile: SubmissionRequestFile,
    ) {
        val requestFileSlot = slot<SubmissionRequestFile>()
        val file = tempFolder.createFile("requested.txt")
        val extFile = NfsFile("dummy.txt", "Files/dummy.txt", file, file.absolutePath, "NOT_CALCULATED", -1)
        val sub = basicExtSubmission.copy(section = ExtSection(type = "Study", files = listOf(left(extFile))))

        every { pendingRequest.submission } returns sub
        every { requestService.getPendingRequest("S-BSST0", 1) } returns pendingRequest
        every { requestService.updateRequestTotalFiles("S-BSST0", 1, 1) } answers { nothing }
        every { requestService.updateRequestStatus("S-BSST0", 1, INDEXED) } answers { nothing }
        every { filesRequestService.upsertSubmissionRequestFile(capture(requestFileSlot)) } answers { nothing }
        every {
            filesRequestService.getSubmissionRequestFiles("S-BSST0", 1, 0)
        } returns sequenceOf(mockRequestFile).asStream()

        testInstance.indexRequest("S-BSST0", 1)

        val requestFile = requestFileSlot.captured
        assertThat(requestFile.index).isEqualTo(1)

        verify(exactly = 1) {
            requestService.getPendingRequest("S-BSST0", 1)
            requestService.updateRequestTotalFiles("S-BSST0", 1, 1)
            requestService.updateRequestStatus("S-BSST0", 1, INDEXED)
            filesRequestService.upsertSubmissionRequestFile(requestFile)
        }
    }
}
