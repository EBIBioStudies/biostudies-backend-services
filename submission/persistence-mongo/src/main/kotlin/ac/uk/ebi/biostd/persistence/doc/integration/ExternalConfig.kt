package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.service.ExtSubmissionRepository
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoPersistenceService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(MongoDbServicesConfig::class, ToDocSubmissionConfig::class)
class ExternalConfig {
    @Bean
    internal fun submissionPersistenceService(
        submissionRepository: ExtSubmissionRepository,
        submissionDocDataRepo: SubmissionDocDataRepository,
    ): SubmissionPersistenceService = SubmissionMongoPersistenceService(submissionRepository, submissionDocDataRepo)

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
