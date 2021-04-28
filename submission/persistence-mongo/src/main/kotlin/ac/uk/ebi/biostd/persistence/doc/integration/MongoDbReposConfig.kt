package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.doc.MongoDbConfig
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionDraftRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestRepository
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
        mongoTemplate: MongoTemplate,
        submissionMongoRepository: SubmissionMongoRepository
    ): SubmissionDocDataRepository = SubmissionDocDataRepository(submissionMongoRepository, mongoTemplate)

    @Bean
    internal fun submissionStatsDataRepository(
        mongoTemplate: MongoTemplate,
        submissionMongoRepository: SubmissionMongoRepository
    ): SubmissionStatsDataRepository = SubmissionStatsDataRepository(mongoTemplate, submissionMongoRepository)

    @Bean
    internal fun submissionRequestDocDataRepository(
        mongoTemplate: MongoTemplate,
        submissionRequestRepository: SubmissionRequestRepository
    ): SubmissionRequestDocDataRepository =
        SubmissionRequestDocDataRepository(submissionRequestRepository, mongoTemplate)

    @Bean
    internal fun submissionDraftDocDataRepository(
        mongoTemplate: MongoTemplate,
        submissionDraftRepository: SubmissionDraftRepository
    ): SubmissionDraftDocDataRepository = SubmissionDraftDocDataRepository(submissionDraftRepository, mongoTemplate)

    @Bean
    internal fun fileListDocFileDocDataRepository(
        mongoTemplate: MongoTemplate,
        fileListDocFileRepository: FileListDocFileRepository
    ): FileListDocFileDocDataRepository = FileListDocFileDocDataRepository(fileListDocFileRepository, mongoTemplate)
}
