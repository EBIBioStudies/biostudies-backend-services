package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.doc.MongoDbConfig
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoMetaQueryService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
@Import(MongoDbConfig::class)
@ConditionalOnProperty(prefix = "app.persistence", name = ["enableMongo"], havingValue = "true")
class MongoDbReposConfig {

    @Bean
    internal fun submissionDocDataRepository(
        submissionRepository: SubmissionMongoRepository,
        mongoTemplate: MongoTemplate
    ): SubmissionDocDataRepository = SubmissionDocDataRepository(submissionRepository, mongoTemplate)

    @Bean
    internal fun submissionRequestDocDataRepository(
        submissionRequestRepository: SubmissionRequestRepository
    ): SubmissionRequestDocDataRepository = SubmissionRequestDocDataRepository(submissionRequestRepository)

    @Bean
    internal fun submissionMetaQueryService(): SubmissionMongoMetaQueryService = SubmissionMongoMetaQueryService()
}
