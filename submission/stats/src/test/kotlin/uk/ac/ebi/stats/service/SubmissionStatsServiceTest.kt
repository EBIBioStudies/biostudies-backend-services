package uk.ac.ebi.stats.service

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
import uk.ac.ebi.stats.exception.StatNotFoundException
import uk.ac.ebi.stats.model.SubmissionStat
import uk.ac.ebi.stats.model.SubmissionStatType.NUMBER_VIEWS
import uk.ac.ebi.stats.persistence.model.SubmissionStatDb
import uk.ac.ebi.stats.persistence.repositories.SubmissionStatsRepository

@ExtendWith(MockKExtension::class)
class SubmissionStatsServiceTest(@MockK private val statsRepository: SubmissionStatsRepository) {
    private val testInstance = SubmissionStatsService(statsRepository)
    private val testStat = SubmissionStatDb("S-TEST123", 10, NUMBER_VIEWS)

    @BeforeEach
    fun beforeEach() {
        every { statsRepository.findAllByType(NUMBER_VIEWS) } returns listOf(testStat)
        every { statsRepository.getByAccNoAndType("S-TEST123", NUMBER_VIEWS) } returns testStat
        every { statsRepository.findByAccNoAndType("S-TEST123", NUMBER_VIEWS) } returns testStat
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `find by type`() {
        val stats = testInstance.findByType(NUMBER_VIEWS)

        assertThat(stats).hasSize(1)
        assertThat(stats.first().accNo).isEqualTo("S-TEST123")
        assertThat(stats.first().value).isEqualTo(10)
        assertThat(stats.first().type).isEqualTo(NUMBER_VIEWS)
    }

    @Test
    fun `find by accNo and type`() {
        val stat = testInstance.findByAccNoAndType("S-TEST123", NUMBER_VIEWS)

        assertThat(stat.accNo).isEqualTo("S-TEST123")
        assertThat(stat.value).isEqualTo(10)
        assertThat(stat.type).isEqualTo(NUMBER_VIEWS)
    }

    @Test
    fun `stat not found`() {
        every { statsRepository.findByAccNoAndType("S-TEST1234", NUMBER_VIEWS) } returns null

        val error = assertThrows<StatNotFoundException> { testInstance.findByAccNoAndType("S-TEST1234", NUMBER_VIEWS) }
        assertThat(error.message).isEqualTo(
            "There is no submission stat registered with AccNo S-TEST1234 and type NUMBER_VIEWS")
    }

    @Test
    fun `save new`() {
        val dbStat = slot<SubmissionStatDb>()
        val stat = SubmissionStat("S-TEST123", 10, NUMBER_VIEWS)

        every { statsRepository.save(capture(dbStat)) } returns testStat
        every { statsRepository.existsByAccNoAndType("S-TEST123", NUMBER_VIEWS) } returns false

        val newStat = testInstance.save(stat)
        assertThat(newStat.accNo).isEqualTo("S-TEST123")
        assertThat(newStat.value).isEqualTo(10)
        assertThat(newStat.type).isEqualTo(NUMBER_VIEWS)
        verify(exactly = 1) { statsRepository.save(dbStat.captured) }
        verify(exactly = 1) { statsRepository.existsByAccNoAndType("S-TEST123", NUMBER_VIEWS) }
    }

    @Test
    fun `save existing`() {
        val dbStat = slot<SubmissionStatDb>()
        val stat = SubmissionStat("S-TEST123", 30, NUMBER_VIEWS)

        every { statsRepository.existsByAccNoAndType("S-TEST123", NUMBER_VIEWS) } returns true
        every { statsRepository.save(capture(dbStat)) } returns SubmissionStatDb("S-TEST123", 30, NUMBER_VIEWS)

        val updatedStat = testInstance.save(stat)
        assertThat(updatedStat.accNo).isEqualTo("S-TEST123")
        assertThat(updatedStat.value).isEqualTo(30)
        assertThat(updatedStat.type).isEqualTo(NUMBER_VIEWS)
        verify(exactly = 1) { statsRepository.save(dbStat.captured) }
        verify(exactly = 1) { statsRepository.getByAccNoAndType("S-TEST123", NUMBER_VIEWS) }
        verify(exactly = 1) { statsRepository.existsByAccNoAndType("S-TEST123", NUMBER_VIEWS) }
    }
}
