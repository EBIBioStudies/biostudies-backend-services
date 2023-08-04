package ac.uk.ebi.biostd.stats.domain.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import ac.uk.ebi.biostd.submission.domain.helpers.TempFileGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionQueryService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.multipart.MultipartFile
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.fileSequence
import java.io.File

@ExtendWith(MockKExtension::class)
class SubmissionStatsServiceTest(
    @MockK private val statsFileHandler: StatsFileHandler,
    @MockK private val tempFileGenerator: TempFileGenerator,
    @MockK private val queryService: ExtSubmissionQueryService,
    @MockK private val submissionStatsService: StatsDataService,
    @MockK private val serializationService: ExtSerializationService,
) {
    private val testInstance = SubmissionStatsService(
        statsFileHandler,
        tempFileGenerator,
        submissionStatsService,
        serializationService,
        queryService,
    )

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

    @Test
    fun `calculate sub files size`(
        @MockK file1: ExtFile,
        @MockK file2: ExtFile,
        @MockK stat: SubmissionStat,
        @MockK submission: ExtSubmission,
    ) {
        val savedStatSlot = slot<SubmissionStat>()

        mockkStatic("uk.ac.ebi.extended.serialization.service.ExtSerializationServiceExtKt")

        every { file1.size } returns 2L
        every { file2.size } returns 3L
        every { submission.accNo } returns "S-BIAD123"
        every { submissionStatsService.save(capture(savedStatSlot)) } returns stat
        every { serializationService.fileSequence(submission) } returns sequenceOf(file1, file2)
        every { queryService.getExtendedSubmission("S-BIAD123", includeFileListFiles = true) } returns submission

        val result = testInstance.calculateSubFilesSize("S-BIAD123")
        val savedStat = savedStatSlot.captured

        assertThat(result).isEqualTo(stat)
        assertThat(savedStat.value).isEqualTo(5L)
        assertThat(savedStat.type).isEqualTo(FILES_SIZE)
        assertThat(savedStat.accNo).isEqualTo("S-BIAD123")
    }
}
