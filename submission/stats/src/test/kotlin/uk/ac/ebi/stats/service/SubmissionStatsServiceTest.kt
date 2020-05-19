package uk.ac.ebi.stats.service

import ac.uk.ebi.biostd.persistence.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.filter.PaginationFilter
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
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
import uk.ac.ebi.stats.model.SubmissionStatType.VIEWS
import uk.ac.ebi.stats.persistence.model.SubmissionStatDb
import uk.ac.ebi.stats.persistence.repositories.SubmissionStatsRepository

@ExtendWith(MockKExtension::class)
class SubmissionStatsServiceTest(
    @MockK private val queryService: SubmissionQueryService,
    @MockK private val statsRepository: SubmissionStatsRepository
) {
    private val testStat = SubmissionStatDb("S-TEST123", 10, VIEWS)
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
        assertTestStat(stats.first(), value = 10)
        assertThat(page.captured.offset).isEqualTo(2)
        assertThat(page.captured.pageSize).isEqualTo(1)
    }

    @Test
    fun `find by accNo and type`() {
        val stat = testInstance.findByAccNoAndType("S-TEST123", VIEWS)
        assertTestStat(stat, value = 10)
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
        val dbStat = slot<SubmissionStatDb>()
        val stat = SubmissionStat("S-TEST123", 10, VIEWS)

        every { statsRepository.save(capture(dbStat)) } returns testStat
        every { statsRepository.existsByAccNoAndType("S-TEST123", VIEWS) } returns false

        val newStat = testInstance.save(stat)
        assertTestStat(newStat, value = 10)
        verify(exactly = 1) { statsRepository.save(dbStat.captured) }
        verify(exactly = 1) { statsRepository.existsByAccNoAndType("S-TEST123", VIEWS) }
    }

    @Test
    fun `save existing`() {
        val dbStat = slot<SubmissionStatDb>()
        val stat = SubmissionStat("S-TEST123", 30, VIEWS)

        every { statsRepository.existsByAccNoAndType("S-TEST123", VIEWS) } returns true
        every { statsRepository.save(capture(dbStat)) } returns SubmissionStatDb("S-TEST123", 30, VIEWS)

        val updatedStat = testInstance.save(stat)
        assertTestStat(updatedStat, value = 30)
        verify(exactly = 1) { statsRepository.save(dbStat.captured) }
        verify(exactly = 1) { statsRepository.getByAccNoAndType("S-TEST123", VIEWS) }
        verify(exactly = 1) { statsRepository.existsByAccNoAndType("S-TEST123", VIEWS) }
    }

    @Test
    fun `save batch`() {
        val dbStat = slot<SubmissionStatDb>()
        val stat = SubmissionStat("S-TEST123", 10, VIEWS)

        every { statsRepository.save(capture(dbStat)) } returns testStat
        every { statsRepository.existsByAccNoAndType("S-TEST123", VIEWS) } returns false

        val newStats = testInstance.saveAll(listOf(stat))
        assertThat(newStats).hasSize(1)
        assertTestStat(newStats.first(), value = 10)
        verify(exactly = 1) { statsRepository.save(dbStat.captured) }
        verify(exactly = 1) { statsRepository.existsByAccNoAndType("S-TEST123", VIEWS) }
    }

    @Test
    fun `save not existing`() {
        every { queryService.existByAccNo("S-TEST123") } returns false
        assertThrows<SubmissionNotFoundException> { testInstance.save(SubmissionStat("S-TEST123", 10, VIEWS)) }
    }

    private fun assertTestStat(stat: SubmissionStat, value: Long) {
        assertThat(stat.value).isEqualTo(value)
        assertThat(stat.accNo).isEqualTo("S-TEST123")
        assertThat(stat.type).isEqualTo(VIEWS)
    }
}
