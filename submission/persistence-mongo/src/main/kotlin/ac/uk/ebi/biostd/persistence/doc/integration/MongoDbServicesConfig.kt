package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestFilesDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.service.CollectionMongoDataService
import ac.uk.ebi.biostd.persistence.doc.service.DistributedLockService
import ac.uk.ebi.biostd.persistence.doc.service.StatsMongoDataService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionRequestFilesMongoPersistenceService
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
    ToExtSubmissionConfig::class,
)
class MongoDbServicesConfig {
    @Bean
    internal fun submissionQueryService(
        submissionDocDataRepository: SubmissionDocDataRepository,
        submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
        serializationService: ExtSerializationService,
        toExtSubmissionMapper: ToExtSubmissionMapper,
    ): SubmissionPersistenceQueryService =
        SubmissionMongoPersistenceQueryService(
            submissionDocDataRepository,
            toExtSubmissionMapper,
            serializationService,
            submissionRequestDocDataRepository,
        )

    @Bean
    internal fun submissionFilesPersistenceService(
        fileListDocFileRepository: FileListDocFileDocDataRepository,
    ): SubmissionFilesPersistenceService = SubmissionMongoFilesPersistenceService(fileListDocFileRepository)

    @Bean
    internal fun submissionRequestPersistenceService(
        serializationService: ExtSerializationService,
        requestRepo: SubmissionRequestDocDataRepository,
        requestFilesRepository: SubmissionRequestFilesDocDataRepository,
        distributedLockService: DistributedLockService,
    ): SubmissionRequestPersistenceService =
        SubmissionRequestMongoPersistenceService(
            serializationService,
            requestRepo,
            requestFilesRepository,
            distributedLockService,
        )

    @Bean
    internal fun submissionRequestFilesPersistenceService(
        extSerializationService: ExtSerializationService,
        requestRepository: SubmissionRequestDocDataRepository,
        requestFilesRepository: SubmissionRequestFilesDocDataRepository,
    ): SubmissionRequestFilesPersistenceService =
        SubmissionRequestFilesMongoPersistenceService(
            extSerializationService,
            requestRepository,
            requestFilesRepository,
        )

    @Bean
    internal fun projectDataService(submissionDocDataRepository: SubmissionDocDataRepository): CollectionDataService =
        CollectionMongoDataService(submissionDocDataRepository)

    @Bean
    internal fun statsDataService(
        submissionsRepository: SubmissionDocDataRepository,
        statsDataRepository: SubmissionStatsDataRepository,
    ): StatsDataService = StatsMongoDataService(submissionsRepository, statsDataRepository)

    @Bean
    fun fileProcessingService(
        serializationService: ExtSerializationService,
        fileResolver: FilesResolver,
    ) = FileProcessingService(serializationService, fileResolver)
}
