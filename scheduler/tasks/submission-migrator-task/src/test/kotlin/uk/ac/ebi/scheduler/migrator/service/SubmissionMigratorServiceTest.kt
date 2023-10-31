package uk.ac.ebi.scheduler.migrator.service

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionMigratorRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.MigrationData
import ebi.ac.uk.extended.model.StorageMode.FIRE
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.scheduler.migrator.config.ApplicationProperties

@ExtendWith(MockKExtension::class)
class SubmissionMigratorServiceTest(
    @MockK private val properties: ApplicationProperties,
    @MockK private val bioWebClient: BioWebClient,
    @MockK private val migratorRepository: SubmissionMigratorRepository,
) {
    private val testInstance = SubmissionMigratorService(properties, bioWebClient, migratorRepository)

    @BeforeEach
    fun beforeEach() {
        setUpProperties()
        setUpPersistence()
    }

    @Test
    fun `migrate submissions`() = runTest {
        every { bioWebClient.transferSubmission(ACC_NO, FIRE) } answers { nothing }

        testInstance.migrateSubmissions()

        verify(exactly = 1) { bioWebClient.transferSubmission(ACC_NO, FIRE) }
    }

    private fun setUpProperties() {
        every { properties.await } returns AWAIT
        every { properties.delay } returns DELAY
        every { properties.concurrency } returns CONCURRENCY
        every { properties.accNoPattern } returns ACC_NO_PATTERN
    }

    private fun setUpPersistence() {
        coEvery { migratorRepository.isMigrated(ACC_NO) } returnsMany listOf(false, true)
        coEvery { migratorRepository.findReadyToMigrate(ACC_NO_PATTERN) } returns listOf(MigrationData(ACC_NO)).asFlow()
    }

    companion object {
        const val DELAY = 1L
        const val AWAIT = 3L
        const val CONCURRENCY = 2
        const val ACC_NO = "E-GEOD-123"
        const val ACC_NO_PATTERN = "E-GEOD-"
    }
}
