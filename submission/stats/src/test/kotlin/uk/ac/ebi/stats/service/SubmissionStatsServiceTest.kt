package uk.ac.ebi.stats.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.filter.PaginationFilter
import ac.uk.ebi.biostd.persistence.model.DbSubmissionStat
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsDataRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.ac.ebi.stats.exception.StatNotFoundException
import uk.ac.ebi.stats.model.SubmissionStat

@ExtendWith(MockKExtension::class)
class SubmissionStatsServiceTest(
    @MockK private val queryService: SubmissionQueryService,
    @MockK private val statsRepository: SubmissionStatsDataRepository
) {
    private val testStat = DbSubmissionStat("S-TEST123", 10, VIEWS)
    private val testInstance = SubmissionStatsService(queryService, statsRepository)

    @BeforeEach
    fun beforeEach() {
        every { queryService.existByAccNo("S-TEST123") } returns true
        every { statsRepository.getByAccNoAndType("S-TEST123", VIEWS) } returns testStat
        every { statsRepository.findByAccNoAndType("S-TEST123", VIEWS) } returns testStat
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `find by type`() {
        val page = slot<PageRequest>()
        val filter = PaginationFilter(limit = 1, offset = 2)
        every { statsRepository.findAllByType(VIEWS, capture(page)) } returns PageImpl(listOf(testStat))

        val stats = testInstance.findByType(VIEWS, filter)
        assertThat(stats).hasSize(1)
        assertTestStat(stats.first(), "S-TEST123", 10)
        assertThat(page.captured.offset).isEqualTo(2)
        assertThat(page.captured.pageSize).isEqualTo(1)
    }

    @Test
    fun `find by accNo and type`() {
        val stat = testInstance.findByAccNoAndType("S-TEST123", VIEWS)
        assertTestStat(stat, "S-TEST123", 10)
    }

    @Test
    fun `stat not found`() {
        every { statsRepository.findByAccNoAndType("S-TEST1234", VIEWS) } returns null

        val error = assertThrows<StatNotFoundException> { testInstance.findByAccNoAndType("S-TEST1234", VIEWS) }
        assertThat(error.message).isEqualTo(
            "There is no submission stat registered with AccNo S-TEST1234 and type VIEWS")
    }

    @Test
    fun `save new`() {
        val dbStat = slot<DbSubmissionStat>()
        val stat = SubmissionStat("S-TEST123", 10, VIEWS)

        every { statsRepository.save(capture(dbStat)) } returns testStat
        every { statsRepository.findByAccNoAndType("S-TEST123", VIEWS) } returns null

        val newStat = testInstance.save(stat)
        assertTestStat(newStat, "S-TEST123", 10)
        verify(exactly = 1) { statsRepository.save(dbStat.captured) }
        verify(exactly = 1) { statsRepository.findByAccNoAndType("S-TEST123", VIEWS) }
    }

    @Test
    fun `save existing`() {
        val dbStat = slot<DbSubmissionStat>()
        val stat = SubmissionStat("S-TEST123", 30, VIEWS)

        every { statsRepository.findByAccNoAndType("S-TEST123", VIEWS) } returns testStat
        every { statsRepository.save(capture(dbStat)) } returns DbSubmissionStat("S-TEST123", 30, VIEWS)

        val updatedStat = testInstance.save(stat)
        assertTestStat(updatedStat, "S-TEST123", 30)
        verify(exactly = 1) { statsRepository.save(dbStat.captured) }
        verify(exactly = 1) { statsRepository.findByAccNoAndType("S-TEST123", VIEWS) }
    }

    @Test
    fun `save batch`() {
        val dbStat = slot<List<DbSubmissionStat>>()
        val stat = SubmissionStat("S-TEST123", 10, VIEWS)

        every { statsRepository.saveAll(capture(dbStat)) } returns listOf(testStat)
        every { statsRepository.findByAccNoAndType("S-TEST123", VIEWS) } returns null

        val newStats = testInstance.saveAll(listOf(stat))
        assertThat(newStats).hasSize(1)
        assertTestStat(newStats.first(), "S-TEST123", 10)
        verify(exactly = 1) { statsRepository.saveAll(dbStat.captured) }
        verify(exactly = 1) { statsRepository.findByAccNoAndType("S-TEST123", VIEWS) }
    }

    @Test
    fun `increment existing`() {
        val statSlot = slot<List<DbSubmissionStat>>()
        val stat = SubmissionStat("S-TEST123", 5, VIEWS)
        val statDb = DbSubmissionStat("S-TEST123", 10, VIEWS)

        every { statsRepository.existsByAccNoAndType("S-TEST123", VIEWS) } returns true
        every { statsRepository.findByAccNoAndType("S-TEST123", VIEWS) } returns statDb
        every { statsRepository.saveAll(capture(statSlot)) } returns listOf(DbSubmissionStat("S-TEST123", 15, VIEWS))

        val incremented = testInstance.incrementAll(listOf(stat))
        assertThat(incremented).hasSize(1)
        assertTestStat(incremented.first(), "S-TEST123", 15)
        assertThat(statSlot.captured.first().value).isEqualTo(15)
    }

    @Test
    fun `increment non existing`() {
        val statSlot = slot<List<DbSubmissionStat>>()
        val stat = SubmissionStat("S-TEST123", 14, VIEWS)

        every { statsRepository.findByAccNoAndType("S-TEST123", VIEWS) } returns null
        every { statsRepository.existsByAccNoAndType("S-TEST123", VIEWS) } returns false
        every { statsRepository.saveAll(capture(statSlot)) } returns listOf(DbSubmissionStat("S-TEST123", 14, VIEWS))

        val incremented = testInstance.incrementAll(listOf(stat))
        assertThat(incremented).hasSize(1)
        assertTestStat(incremented.first(), "S-TEST123", 14)
        assertThat(statSlot.captured.first().value).isEqualTo(14)
    }

    @Test
    fun `save stat for not existing submission`() {
        every { queryService.existByAccNo("S-TEST123") } returns false
        assertThrows<SubmissionNotFoundException> { testInstance.save(SubmissionStat("S-TEST123", 10, VIEWS)) }
    }

    @Test
    fun `save batch for non existing submission`() {
        val statSlot = slot<List<DbSubmissionStat>>()
        val stats = listOf(SubmissionStat("S-TEST123", 10, VIEWS), SubmissionStat("S-TEST124", 20, VIEWS))

        every { queryService.existByAccNo("S-TEST123") } returns true
        every { queryService.existByAccNo("S-TEST124") } returns false
        every { statsRepository.findByAccNoAndType("S-TEST123", VIEWS) } returns null
        every { statsRepository.saveAll(capture(statSlot)) } returns listOf(DbSubmissionStat("S-TEST123", 10, VIEWS))

        val saved = testInstance.saveAll(stats)
        assertThat(saved).hasSize(1)
        assertTestStat(saved.first(), "S-TEST123", 10)
        assertThat(statSlot.captured.first().value).isEqualTo(10)
    }

    @Test
    fun `increment for non existing submission`() {
        val statSlot = slot<List<DbSubmissionStat>>()
        val existingStatDb = DbSubmissionStat("S-TEST123", 5, VIEWS)
        val stats = listOf(
            SubmissionStat("S-TEST123", 10, VIEWS),
            SubmissionStat("S-TEST124", 20, VIEWS),
            SubmissionStat("S-TEST125", 30, VIEWS))

        every { queryService.existByAccNo("S-TEST123") } returns true
        every { queryService.existByAccNo("S-TEST124") } returns false
        every { queryService.existByAccNo("S-TEST125") } returns false
        every { statsRepository.findByAccNoAndType("S-TEST123", VIEWS) } returns existingStatDb
        every { statsRepository.saveAll(capture(statSlot)) } returns listOf(DbSubmissionStat("S-TEST123", 15, VIEWS))

        val incremented = testInstance.incrementAll(stats)
        assertThat(incremented).hasSize(1)
        assertTestStat(incremented.first(), "S-TEST123", 15)
        assertThat(statSlot.captured.first().value).isEqualTo(15)
    }

    @Test
    fun `increment case insensitive`() {
        val statSlot = slot<List<DbSubmissionStat>>()
        val existingStatDb = DbSubmissionStat("DIXA", 5, VIEWS)
        val stats = listOf(
            SubmissionStat("diXa", 10, VIEWS),
            SubmissionStat("dixa", 20, VIEWS),
            SubmissionStat("DIXA", 30, VIEWS))

        every { queryService.existByAccNo("diXa") } returns true
        every { queryService.existByAccNo("dixa") } returns true
        every { queryService.existByAccNo("DIXA") } returns true
        every { statsRepository.findByAccNoAndType("DIXA", VIEWS) } returns existingStatDb
        every { statsRepository.saveAll(capture(statSlot)) } returns listOf(DbSubmissionStat("DIXA", 65, VIEWS))

        val incremented = testInstance.incrementAll(stats)
        assertThat(incremented).hasSize(1)
        assertTestStat(incremented.first(), "DIXA", 65)
        assertThat(statSlot.captured.first().value).isEqualTo(65)
    }

    private fun assertTestStat(stat: SubmissionStat, accNo: String, value: Long) {
        assertThat(stat.value).isEqualTo(value)
        assertThat(stat.accNo).isEqualTo(accNo)
        assertThat(stat.type).isEqualTo(VIEWS)
    }
}
