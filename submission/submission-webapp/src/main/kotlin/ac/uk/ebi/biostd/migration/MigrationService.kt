package ac.uk.ebi.biostd.migration

import ac.uk.ebi.biostd.common.properties.MigrationProperties
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.MigrationData
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.model.RequestStatus.Companion.PROCESSING_STATUS
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import java.time.Instant
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

class MigrationService(
    private val properties: MigrationProperties,
    private val submissionRepository: SubmissionDocDataRepository,
    private val submissionRequestRepository: SubmissionRequestDocDataRepository,
    private val extSubmissionService: ExtSubmissionService,
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun migrateSubmission() =
        runBlocking {
            migrateSubmissions()
        }

    suspend fun migrateSubmissions() {
        val limit = Instant.now().minus(properties.modifiedBeforeDays.toLong(), ChronoUnit.DAYS)
        submissionRepository
            .findReadyToMigrate(limit)
            .filterNot { submissionRequestRepository.existsByAccNoAndStatusIn(it.accNo, PROCESSING_STATUS) }
            .take(properties.limit)
            .collect { migrateSafely(it) }
    }

    private suspend fun migrateSafely(migrationData: MigrationData) {
        suspend fun migrate() {
            logger.info { "Started migrating submission ${migrationData.accNo} to FIRE" }
            val submissionId = extSubmissionService.transferSubmission(properties.user, migrationData.accNo, FIRE)
            logger.info { "Trigger transfer of submission $submissionId" }
        }

        runCatching { migrate() }
            .onFailure { logger.error(it) { "Failed to migrate submission ${migrationData.accNo} to FIRE" } }
            .onSuccess { logger.info { "Migrated submission ${migrationData.accNo} to FIRE" } }
    }
}
