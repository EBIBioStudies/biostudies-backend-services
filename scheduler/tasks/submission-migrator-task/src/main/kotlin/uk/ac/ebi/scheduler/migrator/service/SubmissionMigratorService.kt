package uk.ac.ebi.scheduler.migrator.service

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionMigratorRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.MigrationData
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.extended.model.StorageMode.FIRE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.scheduler.migrator.config.ApplicationProperties
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds

private val logger = KotlinLogging.logger {}

class SubmissionMigratorService(
    private val properties: ApplicationProperties,
    private val bioWebClient: BioWebClient,
    private val migratorRepository: SubmissionMigratorRepository,
) {
    suspend fun migrateSubmissions() {
        withContext(Dispatchers.Default) {
            migratorRepository
                .findReadyToMigrate(properties.accNoPattern)
                .map { async { migrateSafely(it) } }
                .buffer(properties.concurrency)
                .collect { it.await() }
        }
    }

    private suspend fun migrateSafely(migrationData: MigrationData) {
        suspend fun migrate() {
            logger.info { "Started migrating submission ${migrationData.accNo} to FIRE" }
            bioWebClient.transferSubmission(migrationData.accNo, FIRE)
            waitUntil(
                timeout = ofMinutes(properties.await),
                checkInterval = ofSeconds(properties.delay),
            ) { migratorRepository.isMigrated(migrationData.accNo) }
        }

        runCatching { migrate() }
            .onFailure { logger.error(it) { "Failed to migrate submission ${migrationData.accNo} to FIRE" } }
            .onSuccess { logger.info { "Migrated submission ${migrationData.accNo} to FIRE" } }
    }
}
