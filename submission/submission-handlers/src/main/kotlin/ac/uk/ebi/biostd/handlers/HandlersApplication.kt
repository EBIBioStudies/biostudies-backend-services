package ac.uk.ebi.biostd.handlers

import ac.uk.ebi.biostd.handlers.listeners.LogSubmissionReceiver
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

const val BIOSTUDIES_EXCHANGE = "biostudies-exchange"
const val LOG_QUEUE = "submission-submitted-log-queue"
const val ROUTING_KEY = "bio.submission.published"

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<HandlersApplication>(*args)
}

@SpringBootApplication
@EnableRabbit
class HandlersApplication {

    @Bean
    fun queue(): Queue {
        return Queue(LOG_QUEUE, false)
    }

    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange(BIOSTUDIES_EXCHANGE)
    }

    @Bean
    fun binding(queue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY)
    }

    @Bean
    fun logListener() = LogSubmissionReceiver()
}
