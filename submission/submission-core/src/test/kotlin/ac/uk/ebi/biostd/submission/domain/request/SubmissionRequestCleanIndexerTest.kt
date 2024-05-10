package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode.NFS
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow

@ExtendWith(MockKExtension::class)
class SubmissionRequestCleanIndexerTest(
    @MockK val serializationService: ExtSerializationService,
    @MockK val queryService: SubmissionPersistenceQueryService,
    @MockK val requestService: SubmissionRequestPersistenceService,
    @MockK val fileRqtService: SubmissionRequestFilesPersistenceService,
    @MockK val eventsPublisherService: EventsPublisherService,
    @MockK val newSub: ExtSubmission,
    @MockK val currentSub: ExtSubmission,
) {
    private val requestFileSlot = slot<SubmissionRequestFile>()

    private val testInstance =
        SubmissionRequestCleanIndexer(
            serializationService,
            queryService,
            fileRqtService,
            requestService,
            eventsPublisherService,
        )

    @Test
    fun `when no current submission`() =
        runTest {
            every { newSub.accNo } returns ACC_NO
            every { newSub.version } returns CURRENT_VERSION
            coEvery { queryService.findExtByAccNo(ACC_NO, includeFileListFiles = true) } returns null

            val (conflicted, deprecated) = testInstance.indexRequest(newSub)

            assertThat(conflicted).isZero()
            assertThat(deprecated).isZero()
            verify { serializationService wasNot Called }
        }

    @Nested
    inner class WhenCurrentSubmission {
        @BeforeEach
        fun beforeEach() {
            clearAllMocks()

            every { newSub.accNo } returns ACC_NO
            every { newSub.version } returns NEW_VERSION
            every { newSub.storageMode } returns STORAGE_MODE

            every { currentSub.storageMode } returns STORAGE_MODE
            every { currentSub.accNo } returns ACC_NO
            every { currentSub.version } returns CURRENT_VERSION

            coEvery { queryService.findExtByAccNo(ACC_NO, includeFileListFiles = true) } returns currentSub
        }

        @Test
        fun `when a file has the same md5 but diferent path so file need to be cleaned`(
            @MockK newFile: ExtFile,
            @MockK newRqtFile: SubmissionRequestFile,
            @MockK file: NfsFile,
        ) = runTest {
            coEvery {
                fileRqtService.getSubmissionRequestFiles(
                    ACC_NO,
                    NEW_VERSION,
                    INDEXED,
                )
            } returns flowOf(newRqtFile)

            every { newRqtFile.file } returns newFile
            every { newFile.filePath } returns ONE_PATH
            every { newFile.md5 } returns ONE_MD5

            every { file.md5 } returns ONE_MD5
            every { file.filePath } returns ANOTHER_PATH

            mockkStatic(ExtSerializationService::filesFlow)
            coEvery { serializationService.filesFlow(currentSub) } returns flowOf(file)
            coEvery { fileRqtService.saveSubmissionRequestFile(any()) } coAnswers { nothing }

            val (conflicted, deprecated) = testInstance.indexRequest(newSub)

            assertThat(conflicted).isZero()
            assertThat(deprecated).isOne()
            coVerify { fileRqtService.saveSubmissionRequestFile(capture(requestFileSlot)) }
            val requestFile = requestFileSlot.captured
            assertThat(requestFile.status).isEqualTo(DEPRECATED)
            assertThat(requestFile.file).isEqualTo(file)
        }

        @Test
        fun `when a file of the current submission is re used`(
            @MockK newFile: ExtFile,
            @MockK newRqtFile: SubmissionRequestFile,
            @MockK file: NfsFile,
        ) = runTest {
            coEvery {
                fileRqtService.getSubmissionRequestFiles(
                    ACC_NO,
                    NEW_VERSION,
                    INDEXED,
                )
            } returns flowOf(newRqtFile)

            // Both new file and current submission file is the same
            every { newRqtFile.file } returns newFile
            every { newFile.filePath } returns ONE_PATH
            every { newFile.md5 } returns ONE_MD5
            every { file.md5 } returns ONE_MD5
            every { file.filePath } returns ONE_PATH

            mockkStatic(ExtSerializationService::filesFlow)
            coEvery { serializationService.filesFlow(currentSub) } returns flowOf(file)

            val (conflicted, deprecated) = testInstance.indexRequest(newSub)

            assertThat(conflicted).isZero()
            assertThat(deprecated).isZero()
            coVerify(exactly = 0) { fileRqtService.saveSubmissionRequestFile(any()) }
        }

        @Test
        fun `when a file of the current submission needs to be replaced`(
            @MockK newFile: ExtFile,
            @MockK newRqtFile: SubmissionRequestFile,
            @MockK replacedFile: NfsFile,
        ) = runTest {
            coEvery {
                fileRqtService.getSubmissionRequestFiles(
                    ACC_NO,
                    NEW_VERSION,
                    INDEXED,
                )
            } returns flowOf(newRqtFile)

            every { newRqtFile.file } returns newFile
            every { newFile.filePath } returns ONE_PATH
            every { newFile.md5 } returns ONE_PATH

            every { replacedFile.md5 } returns ANOTHER_MD5
            every { replacedFile.filePath } returns ONE_PATH

            mockkStatic(ExtSerializationService::filesFlow)
            coEvery { serializationService.filesFlow(currentSub) } returns flowOf(replacedFile)
            coEvery { fileRqtService.saveSubmissionRequestFile(any()) } coAnswers { nothing }

            val (conflicted, deprecated) = testInstance.indexRequest(newSub)

            assertThat(conflicted).isOne()
            assertThat(deprecated).isZero()
            coVerify { fileRqtService.saveSubmissionRequestFile(capture(requestFileSlot)) }
            val requestFile = requestFileSlot.captured
            assertThat(requestFile.status).isEqualTo(CONFLICTING)
            assertThat(requestFile.file).isEqualTo(replacedFile)
        }
    }

    companion object {
        const val ACC_NO = "abc"
        const val NEW_VERSION = 2
        const val CURRENT_VERSION = 1
        val STORAGE_MODE = NFS

        const val ONE_PATH = "path1"
        const val ANOTHER_PATH = "path2"

        const val ONE_MD5 = "md51"
        const val ANOTHER_MD5 = "md52"
    }
}
