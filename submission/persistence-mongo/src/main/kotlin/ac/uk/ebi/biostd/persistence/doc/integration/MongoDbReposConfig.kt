package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.doc.MongoDbReactiveConfig
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestFilesDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionDraftRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionStatsRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
@Import(MongoDbReactiveConfig::class, SerializationConfiguration::class)
class MongoDbReposConfig {
    @Bean
    internal fun submissionDocDataRepository(
        reactivateMongoTemplate: ReactiveMongoTemplate,
        submissionMongoRepository: SubmissionMongoRepository,
    ): SubmissionDocDataRepository = SubmissionDocDataRepository(submissionMongoRepository, reactivateMongoTemplate)

    @Bean
    internal fun submissionStatsDataRepository(
        reactivateMongoTemplate: ReactiveMongoTemplate,
        submissionStatsRepository: SubmissionStatsRepository,
    ): SubmissionStatsDataRepository = SubmissionStatsDataRepository(reactivateMongoTemplate, submissionStatsRepository)

    @Bean
    internal fun submissionRequestDocDataRepository(
        reactivateMongoTemplate: ReactiveMongoTemplate,
        extSerializationService: ExtSerializationService,
        submissionRequestRepository: SubmissionRequestRepository,
    ): SubmissionRequestDocDataRepository =
        SubmissionRequestDocDataRepository(
            reactivateMongoTemplate,
            extSerializationService,
            submissionRequestRepository,
        )

    @Bean
    internal fun submissionRequestFilesDocDataRepository(
        submissionRequestFilesRepository: SubmissionRequestFilesRepository,
    ): SubmissionRequestFilesDocDataRepository = SubmissionRequestFilesDocDataRepository(submissionRequestFilesRepository)

    @Bean
    internal fun submissionDraftDocDataRepository(
        reactivateMongoTemplate: ReactiveMongoTemplate,
        submissionDraftRepository: SubmissionDraftRepository,
    ): SubmissionDraftDocDataRepository = SubmissionDraftDocDataRepository(submissionDraftRepository, reactivateMongoTemplate)

    @Bean
    internal fun fileListDocFileDocDataRepository(fileListDocFileRepository: FileListDocFileRepository): FileListDocFileDocDataRepository =
        FileListDocFileDocDataRepository(fileListDocFileRepository)
}
