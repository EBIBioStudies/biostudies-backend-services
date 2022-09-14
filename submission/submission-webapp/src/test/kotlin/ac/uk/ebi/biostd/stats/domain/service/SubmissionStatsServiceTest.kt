package ac.uk.ebi.biostd.stats.domain.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import ac.uk.ebi.biostd.submission.domain.helpers.TempFileGenerator
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.multipart.MultipartFile
import java.io.File

@ExtendWith(MockKExtension::class)
class SubmissionStatsServiceTest(
    @MockK private val statsFileHandler: StatsFileHandler,
    @MockK private val tempFileGenerator: TempFileGenerator,
    @MockK private val submissionStatsService: StatsDataService,
) {
    private val testInstance = SubmissionStatsService(statsFileHandler, tempFileGenerator, submissionStatsService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `find by accNo`(
        @MockK stat: SubmissionStat,
    ) {
        val stats = listOf(stat)
        every { submissionStatsService.findByAccNo("S-BSST0") } returns stats

        assertThat(testInstance.findByAccNo("S-BSST0")).isEqualTo(stats)
        verify(exactly = 1) { submissionStatsService.findByAccNo("S-BSST0") }
    }

    @Test
    fun `find by type`(
        @MockK stat: SubmissionStat,
        @MockK filter: PaginationFilter,
    ) {
        val stats = listOf(stat)
        every { submissionStatsService.findByType(VIEWS, filter) } returns stats

        assertThat(testInstance.findByType("VIEWS", filter)).isEqualTo(stats)
        verify(exactly = 1) { submissionStatsService.findByType(VIEWS, filter) }
    }

    @Test
    fun `find by accNo and type`(
        @MockK stat: SubmissionStat,
    ) {
        every { submissionStatsService.findByAccNoAndType("S-BSST0", VIEWS) } returns stat

        assertThat(testInstance.findByAccNoAndType("S-BSST0", "VIEWS")).isEqualTo(stat)
        verify(exactly = 1) { submissionStatsService.findByAccNoAndType("S-BSST0", VIEWS) }
    }

    @Test
    fun `register single`(
        @MockK stat: SubmissionStat,
    ) {
        every { submissionStatsService.save(stat) } returns stat

        assertThat(testInstance.register(stat)).isEqualTo(stat)
        verify(exactly = 1) { submissionStatsService.save(stat) }
    }

    @Test
    fun `register from file`(
        @MockK file: File,
        @MockK stat: SubmissionStat,
        @MockK multiPartFile: MultipartFile,
    ) {
        val stats = listOf(stat)
        every { submissionStatsService.saveAll(stats) } returns stats
        every { tempFileGenerator.asFile(multiPartFile) } returns file
        every { statsFileHandler.readStats(file, VIEWS) } returns stats

        assertThat(testInstance.register("VIEWS", multiPartFile)).isEqualTo(stats)
        verify(exactly = 1) {
            submissionStatsService.saveAll(stats)
            tempFileGenerator.asFile(multiPartFile)
            statsFileHandler.readStats(file, VIEWS)
        }
    }

    @Test
    fun `increment stats`(
        @MockK file: File,
        @MockK stat: SubmissionStat,
        @MockK multiPartFile: MultipartFile,
    ) {
        val stats = listOf(stat)
        every { tempFileGenerator.asFile(multiPartFile) } returns file
        every { statsFileHandler.readStats(file, VIEWS) } returns stats
        every { submissionStatsService.incrementAll(stats) } returns stats

        assertThat(testInstance.increment("VIEWS", multiPartFile)).isEqualTo(stats)
        verify(exactly = 1) {
            tempFileGenerator.asFile(multiPartFile)
            statsFileHandler.readStats(file, VIEWS)
            submissionStatsService.incrementAll(stats)
        }
    }
}
