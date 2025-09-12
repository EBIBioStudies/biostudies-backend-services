package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.common.events.SUBMISSIONS_ROUTING_KEY
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.service.SecurityService
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfig {
    @Bean
    fun securityTestService(
        userDataRepository: UserDataRepository,
        sequenceDataRepository: SequenceDataRepository,
        securityService: SecurityService,
        securityQueryService: SecurityQueryService,
        accessPermissionRepository: AccessPermissionRepository,
        accessTagDataRepo: AccessTagDataRepo,
    ): SecurityTestService =
        SecurityTestService(
            securityService,
            securityQueryService,
            userDataRepository,
            sequenceDataRepository,
            accessPermissionRepository,
            accessTagDataRepo,
        )

    @Bean(name = ["TestCollectionValidator"])
    fun testCollectionValidator(): TestCollectionValidator = TestCollectionValidator()

    @Bean(name = ["FailCollectionValidator"])
    fun failCollectionValidator(): FailCollectionValidator = FailCollectionValidator()

    @Bean(name = ["DelayCollectionValidator"])
    fun delayCollectionValidator(): DelayCollectionValidator = DelayCollectionValidator()

    @Bean
    fun testMessageService(rabbitTemplate: RabbitTemplate): TestMessageService = TestMessageService(rabbitTemplate)

    @Bean
    fun submissionSubmittedQueue(): Queue = Queue(SUBMISSION_SUBMITTED_QUEUE)

    @Bean
    fun submissionSubmittedBinding(exchange: TopicExchange): Binding =
        BindingBuilder.bind(submissionSubmittedQueue()).to(exchange).with(
            SUBMISSIONS_ROUTING_KEY,
        )

    companion object {
        const val SUBMISSION_SUBMITTED_QUEUE = "testSubmissionQueue"
    }
}
