package uk.ac.ebi.scheduler.migrator.service

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ebi.ac.uk.extended.model.StorageMode.FIRE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.awaitility.Awaitility.await
import uk.ac.ebi.scheduler.migrator.persistence.MigrationData
import uk.ac.ebi.scheduler.migrator.persistence.MigratorRepository
import uk.ac.ebi.scheduler.migrator.persistence.getReadyToMigrate
import java.time.Duration

private val logger = KotlinLogging.logger {}

class SubmissionMigratorService(
    private val bioWebClient: BioWebClient,
    private val migratorRepository: MigratorRepository,
) {
    suspend fun migrateSubmissions() {
        withContext(Dispatchers.Default) {
            migratorRepository
                .getReadyToMigrate()
                .chunked(5)
                .forEach { dataChunk ->
                    dataChunk
                        .map { async { migrateSafely(it) } }
                        .awaitAll()
                }
        }
    }

    // TODO calibrate times
    // TODO if the time comes to an end but the request exists, don't mark it as failed
    private suspend fun migrateSafely(migrationData: MigrationData) {
        fun migrate() {
            logger.info { "Started migrating submission ${migrationData.accNo} to FIRE" }
            bioWebClient.transferSubmission(migrationData.accNo, FIRE)
            await()
                .pollInterval(Duration.ofSeconds(30))
                .atMost(Duration.ofMinutes(30))
                .until { migratorRepository.isMigrated(migrationData.accNo) }
        }

        runCatching { migrate() }
            .onFailure { logger.error(it) { "Failed to migrate submission ${migrationData.accNo} to FIRE" } }
            .onSuccess { logger.info { "Migrated submission ${migrationData.accNo} to FIRE" } }
    }
}
