package ac.uk.ebi.biostd.events

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val BIOSTUDIES_EXCHANGE = "biostudies-exchange"
const val PUBLISH_KEY = "bio.submission.published"

@Configuration
class EventsConfig {

    @Bean
    fun exchange(): TopicExchange = TopicExchange(BIOSTUDIES_EXCHANGE)

    @Bean
    fun eventsService(template: RabbitTemplate, properties: ApplicationProperties) = EventsService(template, properties)
}
