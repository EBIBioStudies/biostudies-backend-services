package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.exception.SequenceNotFoundException
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.MockLockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SqlPersistenceServiceTest(
    @MockK private val sequenceRepository: SequenceDataRepository,
    @MockK private val accessTagsDataRepository: AccessTagDataRepo,
    @MockK private val queryService: SubmissionPersistenceQueryService,
) {
    private val lockExecutor = MockLockExecutor()
    private val testInstance =
        SqlPersistenceService(
            sequenceRepository,
            accessTagsDataRepository,
            lockExecutor,
            queryService,
        )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `get next immediately available sequence value`() {
        val sequence = DbSequence("S-BSST")

        mockSequenceRepository(sequence)
        coEvery { queryService.existByAccNo("S-BSST1") } returns false

        assertThat(testInstance.getSequenceNextValue("S-BSST")).isEqualTo(1)
        verify(exactly = 1) { sequenceRepository.save(sequence) }
    }

    @Test
    fun `get next available sequence`() {
        val sequence = DbSequence("S-BSST")

        mockSequenceRepository(sequence)
        coEvery { queryService.existByAccNo("S-BSST1") } returns true
        coEvery { queryService.existByAccNo("S-BSST2") } returns true
        coEvery { queryService.existByAccNo("S-BSST3") } returns false

        assertThat(testInstance.getSequenceNextValue("S-BSST")).isEqualTo(3)
        verify(exactly = 1) { sequenceRepository.save(sequence) }
    }

    @Test
    fun `sequence not found`() {
        every { sequenceRepository.findByPrefix("S-BIAD") } returns null

        val exception = assertThrows<SequenceNotFoundException> { testInstance.getSequenceNextValue("S-BIAD") }

        assertThat(exception.message).isEqualTo("A sequence for the pattern 'S-BIAD' could not be found")
    }

    private fun mockSequenceRepository(sequence: DbSequence) {
        every { sequenceRepository.findByPrefix("S-BSST") } returns sequence
        every { sequenceRepository.save(sequence) } returns sequence
    }
}
