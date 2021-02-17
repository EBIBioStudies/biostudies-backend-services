package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.ProjectDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.service.StatsMongoDataService
import ac.uk.ebi.biostd.persistence.doc.service.ProjectMongoDataService
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
        toExtSubmissionMapper: ToExtSubmissionMapper
    ): SubmissionQueryService = SubmissionMongoQueryService(
        submissionDocDataRepository,
        submissionRequestDocDataRepository,
        toExtSubmissionMapper)

    @Bean
    internal fun projectDataService(
        submissionDocDataRepository: SubmissionDocDataRepository
    ): ProjectDataService = ProjectMongoDataService(submissionDocDataRepository)

    @Bean
    internal fun toExtSubmissionMapper(): ToExtSubmissionMapper = ToExtSubmissionMapper()

    @Bean
    internal fun submissionDraftMongoService(
        submissionDraftDocDataRepository: SubmissionDraftDocDataRepository,
        submissionQueryService: SubmissionQueryService,
        extSerializationService: ExtSerializationService
    ): SubmissionDraftMongoService = SubmissionDraftMongoService(
        submissionDraftDocDataRepository,
        submissionQueryService,
        extSerializationService)

    @Bean
    internal fun submissionMongoMetaQueryService(
        submissionDocDataRepository: SubmissionDocDataRepository
    ): SubmissionMongoMetaQueryService = SubmissionMongoMetaQueryService(submissionDocDataRepository)

    @Bean
    internal fun statsDataService(
        submissionStatsDataRepository: SubmissionStatsDataRepository
    ): StatsDataService = StatsMongoDataService(submissionStatsDataRepository)
}
