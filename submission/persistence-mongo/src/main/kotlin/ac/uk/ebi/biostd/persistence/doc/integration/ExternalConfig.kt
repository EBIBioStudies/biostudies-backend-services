package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.service.ExtSubmissionRepository
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoPersistenceService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
@Import(MongoDbServicesConfig::class, ToDocSubmissionConfig::class)
class ExternalConfig {
    @Bean
    @Suppress("LongParameterList")
    internal fun submissionRequestService(
        submissionDocDataRepository: SubmissionDocDataRepository,
        submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
        serializationService: ExtSerializationService,
        submissionRepository: ExtSubmissionRepository,
    ): SubmissionPersistenceService {
        return SubmissionMongoPersistenceService(
            submissionDocDataRepository,
            submissionRequestDocDataRepository,
            serializationService,
            submissionRepository
        )
    }

    @Bean
    @Suppress("LongParameterList")
    internal fun extSubmissionRepository(
        subDataRepository: SubmissionDocDataRepository,
        fileListDocFileRepository: FileListDocFileRepository,
        toExtSubmissionMapper: ToExtSubmissionMapper,
        toDocSubmissionMapper: ToDocSubmissionMapper,
    ): ExtSubmissionRepository {
        return ExtSubmissionRepository(
            subDataRepository,
            fileListDocFileRepository,
            toExtSubmissionMapper,
            toDocSubmissionMapper
        )
    }
}
