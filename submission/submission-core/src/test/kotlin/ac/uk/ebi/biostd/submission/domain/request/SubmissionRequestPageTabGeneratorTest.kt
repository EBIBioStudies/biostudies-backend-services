package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestPageTabGeneratorTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val rqt: SubmissionRequest,
    @MockK private val loadedSub: ExtSubmission,
    @MockK private val pageTabService: PageTabService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance = SubmissionRequestPageTabGenerator(pageTabService, requestService, filesRequestService)

    @Test
    fun `generate pagetab`() = runTest {
        val file = tempFolder.createFile("S-BSST1.tsv")
        val requestFileSlot = slot<SubmissionRequestFile>()
        val pageTabFile = NfsFile("S-BSST1.tsv", "S-BSST1.tsv", file, file.absolutePath, file.md5(), file.size())
        val sub = basicExtSubmission.copy(accNo = "S-BSST1", pageTabFiles = listOf(pageTabFile))

        every { rqt.totalFiles } returns 1
        every { rqt.submission } returns loadedSub
        every { loadedSub.owner } returns "John Doe"
        every { rqt.withPageTab(sub, 2, CHANGE_ID) } returns rqt
        coEvery { pageTabService.generatePageTab(loadedSub) } returns sub
        coEvery { requestService.saveRequest(rqt) } returns (sub.accNo to sub.version)
        coEvery { filesRequestService.saveSubmissionRequestFile(capture(requestFileSlot)) } answers { nothing }
        coEvery { requestService.getSubmissionRequest("S-BSST1", 1, LOADED, PROCESS_ID) } returns (CHANGE_ID to rqt)

        testInstance.generatePageTab("S-BSST1", 1, PROCESS_ID)

        val pageTabRequestFile = requestFileSlot.captured
        coVerify(exactly = 1) {
            requestService.saveRequest(rqt)
            pageTabService.generatePageTab(loadedSub)
            filesRequestService.saveSubmissionRequestFile(pageTabRequestFile)
            requestService.getSubmissionRequest("S-BSST1", 1, LOADED, PROCESS_ID)
        }
    }

    private companion object {
        const val CHANGE_ID = "change-id"
        const val PROCESS_ID = "process-id"
    }
}
