package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionFilesRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtFileListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSectionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.service.CollectionMongoDataService
import ac.uk.ebi.biostd.persistence.doc.service.StatsMongoDataService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionDraftMongoPersistenceService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionFilesMongoPersistenceService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionRequestMongoPersistenceService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.serialization.common.FilesResolver

@Configuration
@Import(
    MongoDbReposConfig::class,
    MongoDbQueryConfig::class,
    SerializationConfiguration::class,
    ToSubmissionConfig::class
)
class MongoDbServicesConfig {
    @Bean
    internal fun submissionQueryService(
        submissionDocDataRepository: SubmissionDocDataRepository,
        submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
        serializationService: ExtSerializationService,
        toExtSubmissionMapper: ToExtSubmissionMapper,
    ): SubmissionPersistenceQueryService = SubmissionMongoPersistenceQueryService(
        submissionDocDataRepository,
        toExtSubmissionMapper,
        serializationService,
        submissionRequestDocDataRepository,
    )

    @Bean
    internal fun submissionRequestPersistenceService(
        serializationService: ExtSerializationService,
        requestRepo: SubmissionRequestDocDataRepository,
    ): SubmissionRequestPersistenceService = SubmissionRequestMongoPersistenceService(serializationService, requestRepo)

    @Bean
    internal fun submissionFilesPersistenceService(
        submissionDocDataRepository: SubmissionDocDataRepository,
        fileListDocFileRepository: FileListDocFileRepository,
        submissionFilesRepository: SubmissionFilesRepository,
    ): SubmissionFilesPersistenceService = SubmissionFilesMongoPersistenceService(
        submissionDocDataRepository,
        fileListDocFileRepository,
        submissionFilesRepository,
    )

    @Bean
    internal fun projectDataService(
        submissionDocDataRepository: SubmissionDocDataRepository,
    ): CollectionDataService = CollectionMongoDataService(submissionDocDataRepository)

    @Bean
    internal fun toExtFileListMapper(
        fileListDocFileRepository: FileListDocFileRepository,
        extSerializationService: ExtSerializationService,
        extFilesResolver: FilesResolver,
    ): ToExtFileListMapper = ToExtFileListMapper(fileListDocFileRepository, extSerializationService, extFilesResolver)

    @Bean
    internal fun toExtSectionMapper(
        toExtFileListMapper: ToExtFileListMapper,
    ): ToExtSectionMapper = ToExtSectionMapper(toExtFileListMapper)

    @Bean
    internal fun toExtSubmissionMapper(
        toExtSectionMapper: ToExtSectionMapper,
    ): ToExtSubmissionMapper = ToExtSubmissionMapper(toExtSectionMapper)

    @Bean
    internal fun submissionDraftMongoService(
        submissionDraftDocDataRepository: SubmissionDraftDocDataRepository,
    ): SubmissionDraftPersistenceService = SubmissionDraftMongoPersistenceService(submissionDraftDocDataRepository)

    @Bean
    internal fun statsDataService(
        submissionsRepository: SubmissionDocDataRepository,
        statsDataRepository: SubmissionStatsDataRepository,
    ): StatsDataService = StatsMongoDataService(submissionsRepository, statsDataRepository)

    @Bean
    fun fileProcessingService(serializationService: ExtSerializationService, fileResolver: FilesResolver) =
        FileProcessingService(serializationService, fileResolver)
}
