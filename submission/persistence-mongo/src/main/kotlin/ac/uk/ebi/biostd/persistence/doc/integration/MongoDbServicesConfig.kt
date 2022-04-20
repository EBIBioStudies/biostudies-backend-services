package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
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
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoMetaQueryService
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoQueryService
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.extended.serialization.service.ExtFilesResolver
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
@Import(MongoDbReposConfig::class, SerializationConfiguration::class, ToSubmissionConfig::class)
class MongoDbServicesConfig {
    @Bean
    @Suppress("LongParameterList")
    internal fun submissionQueryService(
        submissionDocDataRepository: SubmissionDocDataRepository,
        submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
        fileListDocFileRepository: FileListDocFileRepository,
        serializationService: ExtSerializationService,
        toExtSubmissionMapper: ToExtSubmissionMapper,
    ): SubmissionQueryService = SubmissionMongoQueryService(
        submissionDocDataRepository,
        submissionRequestDocDataRepository,
        fileListDocFileRepository,
        serializationService,
        toExtSubmissionMapper,
    )

    @Bean
    internal fun projectDataService(
        submissionDocDataRepository: SubmissionDocDataRepository
    ): CollectionDataService = CollectionMongoDataService(submissionDocDataRepository)

    @Bean
    internal fun toExtFileListMapper(
        fileListDocFileRepository: FileListDocFileRepository,
        extSerializationService: ExtSerializationService,
        extFilesResolver: ExtFilesResolver,
    ): ToExtFileListMapper = ToExtFileListMapper(fileListDocFileRepository, extSerializationService, extFilesResolver)

    @Bean
    internal fun toExtSectionMapper(
        toExtFileListMapper: ToExtFileListMapper
    ): ToExtSectionMapper = ToExtSectionMapper(toExtFileListMapper)

    @Bean
    internal fun toExtSubmissionMapper(
        toExtSectionMapper: ToExtSectionMapper
    ): ToExtSubmissionMapper = ToExtSubmissionMapper(toExtSectionMapper)

    @Bean
    internal fun submissionDraftMongoService(
        submissionDraftDocDataRepository: SubmissionDraftDocDataRepository,
        submissionQueryService: SubmissionQueryService,
        serializationService: SerializationService,
        toSubmissionMapper: ToSubmissionMapper
    ): SubmissionDraftService = SubmissionDraftMongoService(
        submissionDraftDocDataRepository,
        submissionQueryService,
        serializationService,
        toSubmissionMapper
    )

    @Bean
    internal fun submissionMongoMetaQueryService(
        submissionDocDataRepository: SubmissionDocDataRepository
    ): SubmissionMongoMetaQueryService = SubmissionMongoMetaQueryService(submissionDocDataRepository)

    @Bean
    internal fun statsDataService(
        submissionStatsDataRepository: SubmissionStatsDataRepository
    ): StatsDataService = StatsMongoDataService(submissionStatsDataRepository)
}
