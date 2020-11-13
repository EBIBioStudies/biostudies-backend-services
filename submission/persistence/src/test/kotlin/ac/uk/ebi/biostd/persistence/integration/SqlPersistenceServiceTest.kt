package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.exception.SequenceNotFoundException
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.MockLockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionSqlPersistenceService
import io.mockk.clearAllMocks
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
    @MockK private val submissionService: SubmissionSqlPersistenceService,
    @MockK private val sequenceRepository: SequenceDataRepository,
    @MockK private val accessTagsDataRepository: AccessTagDataRepo,
    @MockK private val submissionQueryService: SubmissionQueryService
) {
    private val lockExecutor = MockLockExecutor()
    private val testInstance = SqlPersistenceService(
        submissionService, sequenceRepository, accessTagsDataRepository, submissionQueryService, lockExecutor)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `get next immediately available sequence value`() {
        val sequence = Sequence("S-BSST")

        mockSequenceRepository(sequence)
        every { submissionQueryService.existByAccNo("S-BSST0") } returns false

        assertThat(testInstance.getSequenceNextValue("S-BSST")).isEqualTo(0)
        verify(exactly = 1) { sequenceRepository.save(sequence) }
    }

    @Test
    fun `get next available sequence`() {
        val sequence = Sequence("S-BSST")

        mockSequenceRepository(sequence)
        every { submissionQueryService.existByAccNo("S-BSST0") } returns true
        every { submissionQueryService.existByAccNo("S-BSST1") } returns true
        every { submissionQueryService.existByAccNo("S-BSST2") } returns false

        assertThat(testInstance.getSequenceNextValue("S-BSST")).isEqualTo(2)
        verify(exactly = 1) { sequenceRepository.save(sequence) }
    }

    @Test
    fun `sequence not found`() {
        every { sequenceRepository.findByPrefix("S-BIAD") } returns null

        val exception = assertThrows<SequenceNotFoundException> { testInstance.getSequenceNextValue("S-BIAD") }

        assertThat(exception.message).isEqualTo("A sequence for the pattern 'S-BIAD' could not be found")
    }

    private fun mockSequenceRepository(sequence: Sequence) {
        every { sequenceRepository.findByPrefix("S-BSST") } returns sequence
        every { sequenceRepository.save(sequence) } returns sequence
    }
}
