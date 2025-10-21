package uk.ac.ebi.scheduler.stats.persistence

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_COLLECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_COLLECTIONS
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where

class StatsReporterDataRepository(
    private val mongoTemplate: ReactiveMongoTemplate,
) {
    suspend fun calculateNonImagingFilesSize(): Long {
        val filter =
            Criteria().orOperator(
                where(STATS_COLLECTIONS).size(0),
                where(STATS_COLLECTIONS).nin(IMAGING_COLLECTION),
            )
        return calculateFilesSize(filter)
    }

    suspend fun calculateImagingFilesSize(): Long {
        val filter = where(SUB_COLLECTIONS).`in`(IMAGING_COLLECTION)
        return calculateFilesSize(filter)
    }

    private suspend fun calculateFilesSize(filter: Criteria): Long {
        val aggregation =
            newAggregation(
                DocSubmissionStats::class.java,
                match(filter),
                group().sum("\$$STATS_OBJECT_KEY.${FILES_SIZE.value}").`as`(RESULT_KEY),
            )

        return mongoTemplate
            .aggregate(aggregation, Result::class.java)
            .awaitFirstOrNull()
            ?.totalFilesSize ?: 0
    }

    companion object {
        const val IMAGING_COLLECTION = "BioImages"
        const val RESULT_KEY = "totalFilesSize"
        const val STATS_OBJECT_KEY = "stats"
    }
}

data class Result(
    val totalFilesSize: Long,
)
