package uk.ac.ebi.scheduler.stats.persistence

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_COLLECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.lookup
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where

class StatsReporterDataRepository(
    private val mongoTemplate: ReactiveMongoTemplate,
) {
    fun calculateNonImagingFilesSize(): Long {
        val filter = where(SUB_VERSION).gt(0).orOperator(
            where(SUB_COLLECTIONS).size(0),
            where(SUB_COLLECTIONS).elemMatch(where(SUB_ACC_NO).nin(IMAGING_COLLECTION))
        )

        return calculateFilesSize(filter)
    }

    fun calculateImagingFilesSize(): Long {
        val filter = where(SUB_VERSION).gt(0)
            .and(SUB_COLLECTIONS).elemMatch(where(SUB_ACC_NO).`in`(IMAGING_COLLECTION))

        return calculateFilesSize(filter)
    }

    private fun calculateFilesSize(filter: Criteria): Long {
        val aggregation = newAggregation(
            DocSubmission::class.java,
            match(filter),
            lookup(STATS_COLLECTION_KEY, SUB_ACC_NO, SUB_ACC_NO, STATS_LOOKUP_KEY),
            unwind(STATS_LOOKUP_KEY),
            group().sum("$STATS_LOOKUP_KEY.$SUB_STATS.$FILES_SIZE").`as`(RESULT_KEY)
        )

        return mongoTemplate
            .aggregate(aggregation, Result::class.java)
            .blockFirst()
            ?.totalFilesSize ?: 0
    }

    companion object {
        const val IMAGING_COLLECTION = "BioImages"
        const val RESULT_KEY = "totalFilesSize"
        const val STATS_COLLECTION_KEY = "submission_stats"
        const val STATS_LOOKUP_KEY = "submissionStats"
    }
}

data class Result(val totalFilesSize: Long)
