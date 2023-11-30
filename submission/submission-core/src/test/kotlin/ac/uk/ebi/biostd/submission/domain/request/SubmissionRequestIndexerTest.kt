package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
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
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestIndexerTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val pendingRqt: SubmissionRequest,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance = SubmissionRequestIndexer(ExtSerializationService(), requestService, filesRequestService)

    @Test
    fun `index request`() = runTest {
        val changeId = "changeId"
        val requestFileSlot = slot<SubmissionRequestFile>()
        val file = tempFolder.createFile("requested.txt")
        val extFile = NfsFile("dummy.txt", "Files/dummy.txt", file, file.absolutePath, "NOT_CALCULATED", -1)
        val sub = basicExtSubmission.copy(section = ExtSection(type = "Study", files = listOf(left(extFile))))

        every { pendingRqt.submission } returns sub
        coEvery {
            requestService.getSubmissionRequest(
                "S-BSST0",
                1,
                RequestStatus.REQUESTED,
                instanceId
            )
        } returns (changeId to pendingRqt)
        coEvery { requestService.saveRequest(pendingRqt.indexed(1, changeId)) } answers { "S-BSST0" to 1 }
        coEvery { filesRequestService.saveSubmissionRequestFile(capture(requestFileSlot)) } answers { nothing }

        testInstance.indexRequest("S-BSST0", 1, instanceId)

        val requestFile = requestFileSlot.captured
        assertThat(requestFile.index).isEqualTo(1)

        coVerify(exactly = 1) {
            requestService.getSubmissionRequest("S-BSST0", 1, RequestStatus.REQUESTED, instanceId)
            requestService.saveRequest(pendingRqt.indexed(1, changeId))
            filesRequestService.saveSubmissionRequestFile(requestFile)
        }
    }

    private companion object {
        const val instanceId = "biostudies-prod"
    }
}
