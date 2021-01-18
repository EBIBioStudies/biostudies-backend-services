package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.filesystem.FileSystemService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.doc.MongoConfig
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoMetaQueryService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoPersistenceService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoQueryService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.nio.file.Paths

@Configuration
@Import(MongoConfig::class)
@ConditionalOnProperty(prefix = "app.persistence", name = ["mongoEnabled"])
class MongoConfig {

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
    internal fun submissionRequestService(
        submissionDocDataRepository: SubmissionDocDataRepository,
        submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
        systemService: FileSystemService,
        serializationService: ExtSerializationService
    ): SubmissionRequestService {
        return SubmissionMongoPersistenceService(
            submissionDocDataRepository,
            submissionRequestDocDataRepository,
            systemService,
            serializationService
        )
    }

    @Bean
    internal fun toExtSubmissionMapper(applicationProperties: ApplicationProperties): ToExtSubmissionMapper =
        ToExtSubmissionMapper(Paths.get(applicationProperties.submissionPath))

    @Bean
    internal fun submissionQueryService(
        submissionDocDataRepository: SubmissionDocDataRepository,
        toExtSubmissionMapper: ToExtSubmissionMapper
    ): SubmissionQueryService = SubmissionMongoQueryService(submissionDocDataRepository, toExtSubmissionMapper)

    @Bean
    internal fun submissionMetaQueryService(): SubmissionMongoMetaQueryService = SubmissionMongoMetaQueryService()
}
