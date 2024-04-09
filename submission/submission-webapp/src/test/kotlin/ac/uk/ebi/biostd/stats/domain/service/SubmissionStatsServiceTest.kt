package ac.uk.ebi.biostd.stats.domain.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.stats.StatsFileHandler
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow
import java.io.File

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionStatsServiceTest(
    @MockK private val statsFileHandler: StatsFileHandler,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val submissionStatsService: StatsDataService,
    @MockK private val serializationService: ExtSerializationService,
) {
    private val testInstance =
        SubmissionStatsService(
            statsFileHandler,
            submissionStatsService,
            serializationService,
            queryService,
        )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `find by accNo`(
        @MockK stat: SubmissionStat,
    ) = runTest {
        val stats = listOf(stat)
        coEvery { submissionStatsService.findByAccNo("S-BSST0") } returns stats

        assertThat(testInstance.findByAccNo("S-BSST0")).isEqualTo(stats)
        coVerify(exactly = 1) { submissionStatsService.findByAccNo("S-BSST0") }
    }

    @Test
    fun `find by type`(
        @MockK stat: SubmissionStat,
        @MockK filter: PageRequest,
    ) = runTest {
        coEvery { submissionStatsService.findByType(VIEWS, filter) } returns flowOf(stat)

        assertThat(testInstance.findByType("VIEWS", filter).toList()).containsExactly(stat)
        coVerify(exactly = 1) { submissionStatsService.findByType(VIEWS, filter) }
    }

    @Test
    fun `find by accNo and type`(
        @MockK stat: SubmissionStat,
    ) = runTest {
        coEvery { submissionStatsService.findByAccNoAndType("S-BSST0", VIEWS) } returns stat

        assertThat(testInstance.findByAccNoAndType("S-BSST0", "VIEWS")).isEqualTo(stat)
        coVerify(exactly = 1) { submissionStatsService.findByAccNoAndType("S-BSST0", VIEWS) }
    }

    @Test
    fun `register single`(
        @MockK stat: SubmissionStat,
    ) = runTest {
        coEvery { submissionStatsService.save(stat) } returns stat

        assertThat(testInstance.register(stat)).isEqualTo(stat)
        coVerify(exactly = 1) { submissionStatsService.save(stat) }
    }

    @Test
    fun `register from file`(
        @MockK file: File,
        @MockK stat: SubmissionStat,
    ) = runTest {
        val stats = listOf(stat)
        coEvery { submissionStatsService.saveAll(stats) } returns stats
        coEvery { statsFileHandler.readStats(file, VIEWS) } returns stats

        assertThat(testInstance.register("VIEWS", file)).isEqualTo(stats)

        coVerify(exactly = 1) {
            submissionStatsService.saveAll(stats)
            statsFileHandler.readStats(file, VIEWS)
        }
    }

    @Test
    fun `increment stats`(
        @MockK file: File,
        @MockK stat: SubmissionStat,
    ) = runTest {
        val stats = listOf(stat)
        coEvery { statsFileHandler.readStats(file, VIEWS) } returns stats
        coEvery { submissionStatsService.incrementAll(stats) } returns stats

        assertThat(testInstance.increment("VIEWS", file)).isEqualTo(stats)
        coVerify(exactly = 1) {
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
    ) = runTest {
        val savedStatSlot = slot<SubmissionStat>()

        mockkStatic("uk.ac.ebi.extended.serialization.service.ExtSerializationServiceExtKt")

        every { file1.size } returns 2L
        every { file2.size } returns 3L
        every { submission.accNo } returns "S-BIAD123"
        coEvery { submissionStatsService.save(capture(savedStatSlot)) } returns stat
        every { serializationService.filesFlow(submission) } returns flowOf(file1, file2)
        coEvery { queryService.getExtByAccNo("S-BIAD123", includeFileListFiles = true) } returns submission

        val result = testInstance.calculateSubFilesSize("S-BIAD123")
        val savedStat = savedStatSlot.captured

        assertThat(result).isEqualTo(stat)
        assertThat(savedStat.value).isEqualTo(5L)
        assertThat(savedStat.type).isEqualTo(FILES_SIZE)
        assertThat(savedStat.accNo).isEqualTo("S-BIAD123")
    }
}
