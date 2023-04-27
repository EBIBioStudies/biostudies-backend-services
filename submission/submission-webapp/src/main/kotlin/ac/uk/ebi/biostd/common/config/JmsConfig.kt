package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.events.BIOSTUDIES_EXCHANGE
import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

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

    @Bean
    fun myRabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = messageConverter()
        return rabbitTemplate
    }

    @Bean
    fun messageConverter(): MessageConverter = Jackson2JsonMessageConverter(ExtSerializationService.mapper)

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
}
