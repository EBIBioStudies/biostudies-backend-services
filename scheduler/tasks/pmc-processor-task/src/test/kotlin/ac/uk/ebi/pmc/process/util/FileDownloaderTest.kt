package ac.uk.ebi.pmc.process.util

import ac.uk.ebi.pmc.client.PmcApi
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.SUBMISSION_EXTESIONS
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
    val properties: PmcImporterProperties,
    @MockK
    val pmcApi: PmcApi,
) {
    private val testInstance = FileDownloader(properties, pmcApi)

    @Test
    fun downloadFilesWhenError(
        @MockK submission: Submission,
        @MockK file: BioFile,
    ) {
        runTest {
            mockkStatic(SUBMISSION_EXTESIONS)
            val exception = RuntimeException("An http Exception")
            every { submission.allFiles() } returns listOf(file)
            every { submission.accNo } returns "S-EPMC123"
            every { file.path } returns "path"
            every { properties.temp } returns temporaryFolder.root.absolutePath
            coEvery { pmcApi.downloadFileStream("123", "path") } throws exception

            val result = testInstance.downloadFiles(submission)

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).hasMessage("An http Exception")
            coVerify(exactly = 3) { pmcApi.downloadFileStream("123", "path") }
        }
    }
}
