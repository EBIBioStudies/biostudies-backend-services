package ac.uk.ebi.biostd.common.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.SUBMISSIONS_REQUEST_ROUTING_KEY
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

const val SUBMISSION_REQUEST_QUEUE = "submission-request-submitter-queue"

@Configuration
class JmsConfig {
    @Bean
    fun exchange(): TopicExchange = TopicExchange(BIOSTUDIES_EXCHANGE)

    @Bean
    fun requestQueue(): Queue = Queue(SUBMISSION_REQUEST_QUEUE, false)

    @Bean
    fun requestBinding(exchange: TopicExchange): Binding =
        BindingBuilder.bind(requestQueue()).to(exchange).with(SUBMISSIONS_REQUEST_ROUTING_KEY)

    @Bean
    fun myRabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.setMessageConverter(messageConverter())
        return rabbitTemplate
    }

    @Bean
    fun messageConverter(): MessageConverter = Jackson2JsonMessageConverter(ExtSerializationService.mapper)
}
