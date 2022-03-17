package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.CollectionService
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.resources.ext.ExtendedPageMapper
import ebi.ac.uk.extended.mapping.to.ToFileList
import ebi.ac.uk.extended.mapping.to.ToSection
import ebi.ac.uk.extended.mapping.to.ToSubmission
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.net.URI

@Configuration
@Suppress("LongParameterList")
@Import(value = [PersistenceConfig::class, SecurityBeansConfig::class])
class SubmissionConfig(
    private val sourceGenerator: SourceGenerator,
    private val serializationService: SerializationService
) {
    @Bean
    fun submissionService(
        subRepository: SubmissionQueryService,
        serializationService: SerializationService,
        userPrivilegeService: IUserPrivilegesService,
        submissionSubmitter: SubmissionSubmitter,
        eventsPublisherService: EventsPublisherService,
        myRabbitTemplate: RabbitTemplate,
        toSubmission: ToSubmission
    ): SubmissionService = SubmissionService(
        subRepository,
        serializationService,
        userPrivilegeService,
        submissionSubmitter,
        eventsPublisherService,
        myRabbitTemplate,
        toSubmission
    )

    @Bean
    fun extSubmissionService(
        rabbitTemplate: RabbitTemplate,
        submissionSubmitter: SubmissionSubmitter,
        subRepository: SubmissionQueryService,
        userPrivilegeService: IUserPrivilegesService,
        securityQueryService: ISecurityQueryService,
        extSerializationService: ExtSerializationService,
        eventsPublisherService: EventsPublisherService
    ): ExtSubmissionService =
        ExtSubmissionService(
            rabbitTemplate,
            submissionSubmitter,
            subRepository,
            userPrivilegeService,
            securityQueryService,
            extSerializationService,
            eventsPublisherService
        )

    @Bean
    fun projectService(
        collectionSqlDataService: CollectionDataService,
        userPrivilegeService: IUserPrivilegesService
    ): CollectionService = CollectionService(collectionSqlDataService, userPrivilegeService)

    @Bean
    fun submitHandler(
        submissionService: SubmissionService,
        userFilesService: UserFilesService,
        securityQueryService: ISecurityQueryService,
        extSubmissionService: ExtSubmissionService
    ): SubmitWebHandler =
        SubmitWebHandler(
            submissionService,
            extSubmissionService,
            sourceGenerator,
            serializationService,
            userFilesService,
            securityQueryService,
            ToSubmission(ToSection(ToFileList()))
        )

    @Bean
    fun submissionHandler(submissionService: SubmissionService): SubmissionsWebHandler =
        SubmissionsWebHandler(submissionService)

    @Bean
    fun extPageMapper(properties: ApplicationProperties) = ExtendedPageMapper(URI.create(properties.instanceBaseUrl))
}
