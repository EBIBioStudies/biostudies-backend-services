package ac.uk.ebi.biostd.jms

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.SUBMISSIONS_REQUEST_ROUTING_KEY

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
}
