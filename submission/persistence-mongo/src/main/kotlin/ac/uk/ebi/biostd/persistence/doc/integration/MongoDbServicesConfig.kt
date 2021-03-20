package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.service.CollectionMongoDataService
import ac.uk.ebi.biostd.persistence.doc.service.StatsMongoDataService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionDraftMongoService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoMetaQueryService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoQueryService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
@Import(MongoDbReposConfig::class)
@ConditionalOnProperty(prefix = "app.persistence", name = ["enableMongo"], havingValue = "true")
class MongoDbServicesConfig {
    @Bean
    internal fun submissionQueryService(
        submissionDocDataRepository: SubmissionDocDataRepository,
        submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
        serializationService: ExtSerializationService,
        toExtSubmissionMapper: ToExtSubmissionMapper
    ): SubmissionQueryService = SubmissionMongoQueryService(
        submissionDocDataRepository,
        submissionRequestDocDataRepository,
        serializationService,
        toExtSubmissionMapper
    )

    @Bean
    internal fun projectDataService(
        submissionDocDataRepository: SubmissionDocDataRepository
    ): CollectionDataService = CollectionMongoDataService(submissionDocDataRepository)

    @Bean
    internal fun toExtSubmissionMapper(): ToExtSubmissionMapper = ToExtSubmissionMapper()

    @Bean
    internal fun submissionDraftMongoService(
        submissionDraftDocDataRepository: SubmissionDraftDocDataRepository,
        submissionQueryService: SubmissionQueryService,
        serializationService: SerializationService
    ): SubmissionDraftService = SubmissionDraftMongoService(
        submissionDraftDocDataRepository,
        submissionQueryService,
        serializationService
    )

    @Bean
    internal fun submissionMongoMetaQueryService(
        submissionDocDataRepository: SubmissionDocDataRepository
    ): SubmissionMongoMetaQueryService = SubmissionMongoMetaQueryService(submissionDocDataRepository)

    @Bean
    internal fun statsDataService(
        submissionStatsDataRepository: SubmissionStatsDataRepository
    ): StatsDataService = StatsMongoDataService(submissionStatsDataRepository)
}
