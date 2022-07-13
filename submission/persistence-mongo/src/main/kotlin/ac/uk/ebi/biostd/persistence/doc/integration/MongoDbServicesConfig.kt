package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtFileListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSectionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.service.CollectionMongoDataService
import ac.uk.ebi.biostd.persistence.doc.service.StatsMongoDataService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionDraftMongoService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoPersistenceQueryService
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.FileIteratorService
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
    @Suppress("LongParameterList")
    internal fun submissionQueryService(
        submissionDocDataRepository: SubmissionDocDataRepository,
        submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
        fileListDocFileRepository: FileListDocFileRepository,
        serializationService: ExtSerializationService,
        toExtSubmissionMapper: ToExtSubmissionMapper,
    ): SubmissionPersistenceQueryService = SubmissionMongoPersistenceQueryService(
        submissionDocDataRepository,
        submissionRequestDocDataRepository,
        fileListDocFileRepository,
        serializationService,
        toExtSubmissionMapper,
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
        submissionPersistenceQueryService: SubmissionPersistenceQueryService,
        serializationService: SerializationService,
        toSubmissionMapper: ToSubmissionMapper,
    ): SubmissionDraftService = SubmissionDraftMongoService(
        submissionDraftDocDataRepository,
        submissionPersistenceQueryService,
        serializationService,
        toSubmissionMapper
    )

    @Bean
    internal fun statsDataService(
        submissionStatsDataRepository: SubmissionStatsDataRepository,
    ): StatsDataService = StatsMongoDataService(submissionStatsDataRepository)

    @Bean
    fun fileProcessingService(serializationService: ExtSerializationService, fileResolver: FilesResolver) =
        FileProcessingService(serializationService, fileResolver)

    @Bean
    fun fileIteratorService(serializationService: ExtSerializationService) = FileIteratorService(serializationService)
}
