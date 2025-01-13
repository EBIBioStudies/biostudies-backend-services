package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.cluster.web.ClusterExecutor
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.helpers.CollectionService
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestCleanIndexer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestSaver
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestValidator
import ac.uk.ebi.biostd.submission.domain.service.SubmissionRequestDraftService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionService
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.domain.submitter.ExtendedSubmissionSubmitter
import ac.uk.ebi.biostd.submission.domain.submitter.LocalExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.domain.submitter.RemoteExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.domain.submitter.RemoteSubmitterExecutor
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.resources.ext.ExtendedPageMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.events.service.EventsPublisherService
import java.net.URI

@Suppress("LongParameterList")
@Configuration
class SubmissionWebConfig {
    @Bean
    fun remoteSubmitterExecutor(
        appProperties: ApplicationProperties,
        clusterClient: ClusterClient,
    ): RemoteSubmitterExecutor = RemoteSubmitterExecutor(appProperties.submissionTask, clusterClient)

    @Bean
    fun extendedSubmissionSubmitter(
        remoteSubmitterExecutor: RemoteSubmitterExecutor,
        appProperties: ApplicationProperties,
        pageTabService: PageTabService,
        requestService: SubmissionRequestPersistenceService,
        persistenceService: SubmissionPersistenceService,
        requestIndexer: SubmissionRequestIndexer,
        requestLoader: SubmissionRequestLoader,
        requestCleanIndexer: SubmissionRequestCleanIndexer,
        requestValidator: SubmissionRequestValidator,
        requestProcessor: SubmissionRequestProcessor,
        submissionReleaser: SubmissionRequestReleaser,
        submissionCleaner: SubmissionRequestCleaner,
        submissionSaver: SubmissionRequestSaver,
        submissionQueryService: ExtSubmissionQueryService,
        eventsPublisherService: EventsPublisherService,
        submissionStatsService: SubmissionStatsService,
    ): ExtSubmissionSubmitter {
        val local =
            LocalExtSubmissionSubmitter(
                appProperties,
                pageTabService,
                requestService,
                persistenceService,
                requestIndexer,
                requestLoader,
                requestCleanIndexer,
                requestValidator,
                requestProcessor,
                submissionReleaser,
                submissionCleaner,
                submissionSaver,
                submissionQueryService,
                eventsPublisherService,
                submissionStatsService,
            )
        val remote =
            RemoteExtSubmissionSubmitter(
                remoteSubmitterExecutor,
                submissionQueryService,
                requestService,
            )
        return ExtendedSubmissionSubmitter(local, remote, appProperties.submissionTask)
    }

    @Bean
    fun collectionService(
        collectionSqlDataService: CollectionDataService,
        userPrivilegeService: IUserPrivilegesService,
    ): CollectionService = CollectionService(collectionSqlDataService, userPrivilegeService)

    @Bean
    fun submitHandler(
        submissionService: SubmissionService,
        extSubmissionQueryService: ExtSubmissionQueryService,
        fileSourcesService: FileSourcesService,
        serializationService: SerializationService,
        toSubmissionMapper: ToSubmissionMapper,
        queryService: SubmissionMetaQueryService,
        fileServiceFactory: FileServiceFactory,
        requestDraftService: SubmissionRequestDraftService,
    ): SubmitWebHandler =
        SubmitWebHandler(
            submissionService,
            extSubmissionQueryService,
            fileSourcesService,
            serializationService,
            toSubmissionMapper,
            queryService,
            fileServiceFactory,
            requestDraftService,
        )

    @Bean
    fun submissionRequestDraftService(
        toSubmissionMapper: ToSubmissionMapper,
        serializationService: SerializationService,
        userPrivilegesService: IUserPrivilegesService,
        submissionQueryService: SubmissionPersistenceQueryService,
        requestPersistenceService: SubmissionRequestPersistenceService,
    ): SubmissionRequestDraftService =
        SubmissionRequestDraftService(
            toSubmissionMapper,
            serializationService,
            userPrivilegesService,
            submissionQueryService,
            requestPersistenceService,
        )

    @Bean
    fun submissionHandler(
        submissionService: SubmissionService,
        submissionQueryService: SubmissionQueryService,
    ): SubmissionsWebHandler = SubmissionsWebHandler(submissionService, submissionQueryService)

    @Bean
    fun extPageMapper(properties: ApplicationProperties): ExtendedPageMapper = ExtendedPageMapper(URI.create(properties.instanceBaseUrl))

    @Bean
    fun submitRequestBuilder(
        onBehalfUtils: OnBehalfUtils,
        properties: ApplicationProperties,
    ): SubmitRequestBuilder = SubmitRequestBuilder(onBehalfUtils, properties.submissionTask)

    @Bean
    fun onBehalfUtils(securityQueryService: ISecurityQueryService): OnBehalfUtils = OnBehalfUtils(securityQueryService)

    @Bean
    fun clusterExecutor(properties: ApplicationProperties): ClusterExecutor = ClusterExecutor(properties)
}
