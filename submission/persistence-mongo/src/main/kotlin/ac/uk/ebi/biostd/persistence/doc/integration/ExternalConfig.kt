package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.common.filesystem.FileSystemService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoPersistenceService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
@Import(MongoDbServicesConfig::class)
@ConditionalOnProperty(prefix = "app.persistence", name = ["enableMongo"], havingValue = "true")
class ExternalConfig {

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
}
