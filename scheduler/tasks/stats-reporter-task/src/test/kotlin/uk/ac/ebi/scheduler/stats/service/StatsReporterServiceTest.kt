package uk.ac.ebi.scheduler.stats.service

import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.scheduler.stats.persistence.StatsReporterDataRepository
import uk.ac.ebi.scheduler.stats.service.StatsReporterService.Companion.IMAGING_REPORT_NAME
import uk.ac.ebi.scheduler.stats.service.StatsReporterService.Companion.NON_IMAGING_REPORT_NAME
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class StatsReporterServiceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val statsRepository: StatsReporterDataRepository,
) {
    private val testInstance = StatsReporterService(tempFolder.root.toPath(), statsRepository)

    @BeforeEach
    fun beforeEach() {
        setUpDate()
        setUpStats()
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun reportStats() {
        testInstance.reportStats()

        val imagingReport = tempFolder.root.resolve("202307_$IMAGING_REPORT_NAME.txt")
        val expectedImagingReport = "202306\t$PREVIOUS_IMAGING_FILES_SIZE\n202307\t$IMAGING_FILES_SIZE"
        assertThat(imagingReport.exists()).isTrue()
        assertThat(imagingReport).hasContent(expectedImagingReport)

        val nonImagingReport = tempFolder.root.resolve("202307_$NON_IMAGING_REPORT_NAME.txt")
        val expectedNonImagingReport = "202306\t$PREVIOUS_NON_IMAGING_FILES_SIZE\n202307\t$NON_IMAGING_FILES_SIZE"
        assertThat(nonImagingReport.exists()).isTrue()
        assertThat(nonImagingReport).hasContent(expectedNonImagingReport)
    }

    private fun setUpDate() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns OffsetDateTime.of(2023, 8, 24, 0, 0, 0, 0, UTC)
    }

    private fun setUpStats() {
        every { statsRepository.calculateImagingFilesSize() } returns IMAGING_FILES_SIZE
        every { statsRepository.calculateNonImagingFilesSize() } returns NON_IMAGING_FILES_SIZE

        tempFolder.createFile("202306_$IMAGING_REPORT_NAME.txt", "202306\t$PREVIOUS_IMAGING_FILES_SIZE\n")
        tempFolder.createFile("202306_$NON_IMAGING_REPORT_NAME.txt", "202306\t$PREVIOUS_NON_IMAGING_FILES_SIZE\n")
    }

    companion object {
        private const val IMAGING_FILES_SIZE = 68271812L
        private const val PREVIOUS_IMAGING_FILES_SIZE = 68271714L

        private const val NON_IMAGING_FILES_SIZE = 9181927472L
        private const val PREVIOUS_NON_IMAGING_FILES_SIZE = 9181921234L
    }
}
