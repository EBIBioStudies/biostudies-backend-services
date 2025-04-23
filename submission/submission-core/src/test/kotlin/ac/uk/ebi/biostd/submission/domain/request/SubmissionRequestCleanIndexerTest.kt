package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING_PAGE_TAB
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.DEPRECATED_PAGE_TAB
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.REUSED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.PersistedExtFile
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.util.collections.second
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow
import uk.ac.ebi.extended.serialization.service.filesFlowExt

@ExtendWith(MockKExtension::class)
class SubmissionRequestCleanIndexerTest(
    @MockK val newSub: ExtSubmission,
    @MockK val currentSub: ExtSubmission,
    @MockK val serializationService: ExtSerializationService,
    @MockK val queryService: SubmissionPersistenceQueryService,
    @MockK val requestService: SubmissionRequestPersistenceService,
    @MockK val fileRqtService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance =
        SubmissionRequestCleanIndexer(
            serializationService,
            queryService,
            fileRqtService,
            requestService,
        )

    @Test
    fun `when no current submission`() =
        runTest {
            every { newSub.accNo } returns ACC_NO
            every { newSub.version } returns CURRENT_VERSION
            coEvery { queryService.findExtByAccNo(ACC_NO, includeFileListFiles = true) } returns null

            val (previousVersion, requestChanges) = testInstance.indexRequest(newSub)

            assertThat(previousVersion).isNull()
            assertThat(requestChanges.reusedFiles).isZero()
            assertThat(requestChanges.deprecatedFiles).isZero()
            assertThat(requestChanges.deprecatedPageTab).isZero()
            assertThat(requestChanges.conflictingFiles).isZero()
            assertThat(requestChanges.conflictingPageTab).isZero()
            verify { serializationService wasNot Called }
        }

    @Nested
    inner class WhenCurrentSubmission(
        @MockK private val file: NfsFile,
        @MockK private val newFile: PersistedExtFile,
        @MockK private val pageTabFile: NfsFile,
        @MockK private val newPageTabFile: PersistedExtFile,
        @MockK private val newRqtFile: SubmissionRequestFile,
        @MockK private val newPageTabRqtFile: SubmissionRequestFile,
    ) {
        @BeforeEach
        fun beforeEach() {
            clearAllMocks()

            mockkStatic(EXT_SUB_EXT)
            mockkStatic(ExtSerializationService::filesFlow)

            every { newSub.accNo } returns ACC_NO
            every { newSub.version } returns NEW_VERSION
            every { newSub.storageMode } returns NFS

            every { currentSub.storageMode } returns NFS
            every { currentSub.accNo } returns ACC_NO
            every { currentSub.version } returns CURRENT_VERSION

            every { file.md5 } returns FILE_MD5_1
            every { file.filePath } returns FILE_PATH_1

            every { pageTabFile.md5 } returns PAGE_TAB_MD5_1
            every { pageTabFile.filePath } returns PAGE_TAB_PATH

            every { newRqtFile.file } returns newFile
            every { newFile.filePath } returns FILE_PATH_1
            every { newFile.md5 } returns FILE_MD5_1

            every { newPageTabRqtFile.file } returns newPageTabFile
            every { newPageTabFile.filePath } returns PAGE_TAB_PATH
            every { newPageTabFile.md5 } returns PAGE_TAB_MD5_1

            coEvery {
                fileRqtService.getSubmissionRequestFiles(
                    ACC_NO,
                    NEW_VERSION,
                    LOADED,
                )
            } returns flowOf(newRqtFile, newPageTabRqtFile)
            every { newSub.allPageTabFiles } returns listOf(newPageTabFile)
            coEvery { serializationService.filesFlowExt(currentSub) } returns flowOf(false to file, true to pageTabFile)

            coEvery { queryService.findExtByAccNo(ACC_NO, includeFileListFiles = true) } returns currentSub
        }

        @Test
        fun `when a file has the same md5 but different path so file needs to be cleaned`() =
            runTest {
                val requestFileSlots = mutableListOf<SubmissionRequestFile>()
                every { file.filePath } returns FILE_PATH_2
                coEvery { fileRqtService.saveSubmissionRequestFile(capture(requestFileSlots)) } coAnswers { nothing }

                val (previousVersion, requestChanges) = testInstance.indexRequest(newSub)

                assertThat(requestChanges.reusedFiles).isOne()
                assertThat(requestChanges.deprecatedFiles).isOne()
                assertThat(requestChanges.deprecatedPageTab).isZero()
                assertThat(requestChanges.conflictingFiles).isZero()
                assertThat(requestChanges.conflictingPageTab).isZero()
                assertThat(previousVersion).isEqualTo(CURRENT_VERSION)
                assertThat(requestFileSlots).hasSize(2)
                assertThat(requestFileSlots.first().status).isEqualTo(DEPRECATED)
                assertThat(requestFileSlots.first().file).isEqualTo(file)
                assertThat(requestFileSlots.second().status).isEqualTo(REUSED)
                assertThat(requestFileSlots.second().file).isEqualTo(pageTabFile)
            }

        @Test
        fun `when a file has the same md5 and path but different storage mode so file needs to be cleaned`() =
            runTest {
                val requestFileSlots = mutableListOf<SubmissionRequestFile>()
                every { newSub.storageMode } returns FIRE
                every { file.filePath } returns FILE_PATH_2
                coEvery { fileRqtService.saveSubmissionRequestFile(capture(requestFileSlots)) } coAnswers { nothing }

                val (previousVersion, requestChanges) = testInstance.indexRequest(newSub)

                assertThat(requestChanges.reusedFiles).isZero()
                assertThat(requestChanges.deprecatedFiles).isOne()
                assertThat(requestChanges.deprecatedPageTab).isOne()
                assertThat(requestChanges.conflictingFiles).isZero()
                assertThat(requestChanges.conflictingPageTab).isZero()
                assertThat(previousVersion).isEqualTo(CURRENT_VERSION)
                assertThat(requestFileSlots).hasSize(2)
                assertThat(requestFileSlots.first().status).isEqualTo(DEPRECATED)
                assertThat(requestFileSlots.first().file).isEqualTo(file)
                assertThat(requestFileSlots.second().status).isEqualTo(DEPRECATED_PAGE_TAB)
                assertThat(requestFileSlots.second().file).isEqualTo(pageTabFile)
            }

        @Test
        fun `when a file of the current submission is re used`() =
            runTest {
                val requestFileSlots = mutableListOf<SubmissionRequestFile>()
                coEvery { fileRqtService.saveSubmissionRequestFile(capture(requestFileSlots)) } coAnswers { nothing }

                val (previousVersion, requestChanges) = testInstance.indexRequest(newSub)

                assertThat(requestChanges.reusedFiles).isEqualTo(2)
                assertThat(requestChanges.deprecatedFiles).isZero()
                assertThat(requestChanges.deprecatedPageTab).isZero()
                assertThat(requestChanges.conflictingFiles).isZero()
                assertThat(requestChanges.conflictingPageTab).isZero()
                assertThat(previousVersion).isEqualTo(CURRENT_VERSION)
                assertThat(requestFileSlots).hasSize(2)
                assertThat(requestFileSlots.first().status).isEqualTo(REUSED)
                assertThat(requestFileSlots.first().file).isEqualTo(file)
                assertThat(requestFileSlots.second().status).isEqualTo(REUSED)
                assertThat(requestFileSlots.second().file).isEqualTo(pageTabFile)
            }

        @Test
        fun `when a file of the current submission needs to be replaced`() =
            runTest {
                val requestFileSlots = mutableListOf<SubmissionRequestFile>()
                every { file.md5 } returns FILE_MD5_2
                every { newPageTabFile.md5 } returns PAGE_TAB_MD5_2
                coEvery { fileRqtService.saveSubmissionRequestFile(capture(requestFileSlots)) } coAnswers { nothing }

                val (previousVersion, requestChanges) = testInstance.indexRequest(newSub)

                assertThat(requestChanges.reusedFiles).isZero()
                assertThat(requestChanges.deprecatedFiles).isZero()
                assertThat(requestChanges.deprecatedPageTab).isZero()
                assertThat(requestChanges.conflictingFiles).isOne()
                assertThat(requestChanges.conflictingPageTab).isOne()
                assertThat(previousVersion).isEqualTo(CURRENT_VERSION)
                assertThat(requestFileSlots).hasSize(2)
                assertThat(requestFileSlots.first().status).isEqualTo(CONFLICTING)
                assertThat(requestFileSlots.first().file).isEqualTo(file)
                assertThat(requestFileSlots.second().status).isEqualTo(CONFLICTING_PAGE_TAB)
                assertThat(requestFileSlots.second().file).isEqualTo(pageTabFile)
            }
    }

    private companion object {
        const val ACC_NO = "S-BSST1"
        const val CURRENT_VERSION = 1
        const val EXT_SUB_EXT = "ebi.ac.uk.extended.model.ExtSubmissionExtensionsKt"
        const val FILE_MD5_1 = "md51"
        const val FILE_MD5_2 = "md52"
        const val FILE_PATH_1 = "file1.txt"
        const val FILE_PATH_2 = "file2.txt"
        const val NEW_VERSION = 2
        const val PAGE_TAB_MD5_1 = "md53"
        const val PAGE_TAB_MD5_2 = "md54"
        const val PAGE_TAB_PATH = "S-TEST1.tsv"
    }
}
