package ac.uk.ebi.pmc.process.util

import ac.uk.ebi.pmc.client.PmcApi
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.SUBMISSION_EXTENSIONS
import ebi.ac.uk.model.extensions.allFiles
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FileDownloaderTest(
    val temporaryFolder: TemporaryFolder,
    @MockK
    val pmcApi: PmcApi,
) {
    private val testInstance = FileDownloader(pmcApi)

    @Test
    fun downloadFilesWhenError(
        @MockK submission: Submission,
        @MockK file: BioFile,
    ) {
        runTest {
            mockkStatic(SUBMISSION_EXTENSIONS)
            val exception = RuntimeException("An http Exception")
            val targetFolder = temporaryFolder.root
            every { submission.allFiles() } returns listOf(file)
            every { submission.accNo } returns "S-EPMC123"
            every { file.path } returns "path"

            coEvery { pmcApi.downloadFileStream("123", "path") } throws exception

            val result = testInstance.downloadFiles(targetFolder, submission)

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).hasMessage("An http Exception")
            coVerify(exactly = 3) { pmcApi.downloadFileStream("123", "path") }
        }
    }
}
