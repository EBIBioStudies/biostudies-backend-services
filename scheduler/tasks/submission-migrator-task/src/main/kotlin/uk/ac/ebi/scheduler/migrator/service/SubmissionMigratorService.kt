package uk.ac.ebi.scheduler.migrator.service

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ebi.ac.uk.extended.model.StorageMode.FIRE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.awaitility.Awaitility.await
import uk.ac.ebi.scheduler.migrator.config.ApplicationProperties
import uk.ac.ebi.scheduler.migrator.persistence.MigrationData
import uk.ac.ebi.scheduler.migrator.persistence.MigratorRepository
import uk.ac.ebi.scheduler.migrator.persistence.getReadyToMigrate
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds

private val logger = KotlinLogging.logger {}

class SubmissionMigratorService(
    private val properties: ApplicationProperties,
    private val bioWebClient: BioWebClient,
    private val migratorRepository: MigratorRepository,
) {
    suspend fun migrateSubmissions() {
        withContext(Dispatchers.Default) {
            migratorRepository
                .getReadyToMigrate(properties.accNoPattern)
                .map { async { migrateSafely(it) } }
                .buffer(properties.concurrency)
                .collect { it.await() }
        }
    }

    // TODO tests
    private fun migrateSafely(migrationData: MigrationData) {
        fun migrate() {
            logger.info { "Started migrating submission ${migrationData.accNo} to FIRE" }
            bioWebClient.transferSubmission(migrationData.accNo, FIRE)
            await()
                .pollInterval(ofSeconds(properties.delay))
                .atMost(ofMinutes(properties.await))
                .until { migratorRepository.isMigrated(migrationData.accNo) }
        }

        runCatching { migrate() }
            .onFailure { logger.error(it) { "Failed to migrate submission ${migrationData.accNo} to FIRE" } }
            .onSuccess { logger.info { "Migrated submission ${migrationData.accNo} to FIRE" } }
    }
}
