package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.domain.postprocessing.LocalPostProcessingService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionMessageListener
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.events.service.EventsPublisherService

internal const val LISTENER_FACTORY_NAME = "processingListenerFactory"

@Configuration
class JmsConfig(
    private val properties: ApplicationProperties,
) {
    @Bean
    fun exchange(): TopicExchange = TopicExchange(BIOSTUDIES_EXCHANGE)

    @Bean
    fun requestQueue(): Queue = Queue(properties.notifications.requestQueue, false)

    @Bean
    fun requestBinding(exchange: TopicExchange): Binding =
        BindingBuilder.bind(requestQueue()).to(exchange).with(properties.notifications.requestRoutingKey)

    @Bean(name = [LISTENER_FACTORY_NAME])
    fun processingListenerFactory(
        configurer: SimpleRabbitListenerContainerFactoryConfigurer,
        connectionFactory: ConnectionFactory,
        applicationProperties: ApplicationProperties,
    ): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConcurrentConsumers(applicationProperties.consumers)
        factory.setMaxConcurrentConsumers(applicationProperties.maxConsumers)
        configurer.configure(factory, connectionFactory)
        return factory
    }

    @Bean
    fun submissionMessageListener(
        submissionSubmitter: ExtSubmissionSubmitter,
        eventsPublisherService: EventsPublisherService,
        submissionPostProcessingService: LocalPostProcessingService,
    ): SubmissionMessageListener =
        SubmissionMessageListener(
            submissionSubmitter,
            properties.notifications,
            eventsPublisherService,
            submissionPostProcessingService,
        )
}
