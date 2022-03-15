package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocFileList
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSection
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmission
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.service.ExtSubmissionRepository
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import kotlin.io.path.Path

@Configuration
@Import(MongoDbServicesConfig::class)
class ExternalConfig {
    @Bean
    @Suppress("LongParameterList")
    internal fun submissionRequestService(
        submissionDocDataRepository: SubmissionDocDataRepository,
        submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
        serializationService: ExtSerializationService,
        systemService: FileSystemService,
        submissionRepository: ExtSubmissionRepository,
        properties: ApplicationProperties
    ): SubmissionPersistenceService {
        return SubmissionMongoPersistenceService(
            submissionDocDataRepository,
            submissionRequestDocDataRepository,
            serializationService,
            systemService,
            submissionRepository,
            Path(properties.requestFilesPath)
        )
    }

    @Bean
    @Suppress("LongParameterList")
    internal fun extSubmissionRepository(
        subDataRepository: SubmissionDocDataRepository,
        draftDocDataRepository: SubmissionDraftDocDataRepository,
        fileListDocFileRepository: FileListDocFileRepository,
        toExtSubmissionMapper: ToExtSubmissionMapper,
        toDocSubmission: ToDocSubmission
    ): ExtSubmissionRepository {
        return ExtSubmissionRepository(
            subDataRepository,
            draftDocDataRepository,
            fileListDocFileRepository,
            toExtSubmissionMapper,
            toDocSubmission
        )
    }

    @Bean
    internal fun toDocSubmission(toDocSection: ToDocSection): ToDocSubmission = ToDocSubmission(toDocSection)

    @Bean
    internal fun toDocSection(toDocFileList: ToDocFileList): ToDocSection = ToDocSection(toDocFileList)

    @Bean
    internal fun toDocFileList(): ToDocFileList = ToDocFileList()
}
