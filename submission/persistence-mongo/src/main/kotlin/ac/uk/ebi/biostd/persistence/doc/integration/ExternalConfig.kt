package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
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
    @Suppress("LongParameterList")
    internal fun submissionRequestService(
        submissionDocDataRepository: SubmissionDocDataRepository,
        serializationService: ExtSerializationService,
        submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
        submissionDraftDocDataRepository: SubmissionDraftDocDataRepository,
        systemService: FileSystemService,
        fileListDocFileRepository: FileListDocFileDocDataRepository,
        toExtSubmissionMapper: ToExtSubmissionMapper
    ): SubmissionRequestService {
        return SubmissionMongoPersistenceService(
            submissionDocDataRepository,
            submissionRequestDocDataRepository,
            submissionDraftDocDataRepository,
            serializationService,
            systemService,
            fileListDocFileRepository,
            toExtSubmissionMapper
        )
    }
}
