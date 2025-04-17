package uk.ac.ebi.scheduler.pmc.exporter.persistence

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.query.Criteria.where
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

class PmcDataRepository(
    private val pmcRepository: PmcRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) {
    suspend fun createView() =
        withContext(Dispatchers.Default) {
            val job =
                launch {
                    val start = Instant.now()
                    while (isActive) {
                        val elapsed = Duration.between(start, Instant.now()).toSeconds()
                        logger.info { "Elapsed time: $elapsed seconds" }
                        delay(30_000)
                    }
                }

            val aggregation =
                newAggregation(
                    Aggregation.match(where("collections.accNo").`is`("EuropePMC").and("version").gte(0)),
                    Aggregation.project("accNo", "title"),
                    Aggregation.out(PMC_SUBMISSION_VIEW),
                )
            mongoTemplate
                .aggregate(aggregation, DocSubmission::class.java, Map::class.java)
                .then()
                .awaitFirstOrNull()
            job.cancelAndJoin()
        }

    suspend fun findAllFromView(): Flow<PmcData> = mongoTemplate.findAll(PmcData::class.java, PMC_SUBMISSION_VIEW).asFlow()

    fun findAllPmc(): Flow<PmcData> = pmcRepository.findAllPmc()

    companion object {
        val PMC_SUBMISSION_VIEW = "pmc_export_submissions"
    }
}
