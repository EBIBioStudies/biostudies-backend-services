package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.ProjectDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.resources.ext.ExtendedPageMapper
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.events.service.EventsPublisherService
import java.net.URI

@Configuration
@Import(value = [PersistenceConfig::class, SecurityBeansConfig::class])
class SubmissionConfig(
    private val sourceGenerator: SourceGenerator,
    private val serializationService: SerializationService
) {
    @Bean
    @Suppress("LongParameterList")
    fun submissionService(
        subRepository: SubmissionQueryService,
        serializationService: SerializationService,
        userPrivilegeService: IUserPrivilegesService,
        queryService: SubmissionMetaQueryService,
        submissionSubmitter: SubmissionSubmitter,
        eventsPublisherService: EventsPublisherService,
        myRabbitTemplate: RabbitTemplate
    ): SubmissionService = SubmissionService(
        subRepository,
        serializationService,
        userPrivilegeService,
        queryService,
        submissionSubmitter,
        eventsPublisherService,
        myRabbitTemplate)

    @Bean
    fun extSubmissionService(
        submissionRequestService: SubmissionRequestService,
        subRepository: SubmissionQueryService,
        userPrivilegeService: IUserPrivilegesService
    ): ExtSubmissionService = ExtSubmissionService(submissionRequestService, subRepository, userPrivilegeService)

    @Bean
    fun projectService(
        projectSqlDataService: ProjectDataService,
        userPrivilegeService: IUserPrivilegesService
    ): ProjectService = ProjectService(projectSqlDataService, userPrivilegeService)

    @Bean
    fun submitHandler(
        submissionService: SubmissionService,
        userFilesService: UserFilesService,
        securityService: ISecurityService
    ): SubmitWebHandler =
        SubmitWebHandler(
            submissionService,
            sourceGenerator,
            serializationService,
            userFilesService,
            securityService
        )

    @Bean
    fun submissionHandler(submissionService: SubmissionService): SubmissionsWebHandler =
        SubmissionsWebHandler(submissionService)

    @Bean
    fun extPageMapper(properties: ApplicationProperties) = ExtendedPageMapper(URI.create(properties.instanceBaseUrl))
}
