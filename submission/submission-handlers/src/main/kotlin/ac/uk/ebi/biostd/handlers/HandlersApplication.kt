package ac.uk.ebi.biostd.handlers

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

const val BIOSTUDIES_EXCHANGE = "biostudies-exchange"
const val SUBMISSIONS_ROUTING_KEY = "bio.submission.published"

const val LOG_QUEUE = "submission-submitted-log-queue"
const val PARTIALS_QUEUE = "submission-submitted-partials-queue"
const val NOTIFICATIONS_QUEUE = "submission-submitted-notifications-queue"

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<HandlersApplication>(*args)
}

@SpringBootApplication
@EnableRabbit
class HandlersApplication {
    @Bean
    fun logQueue(): Queue = Queue(LOG_QUEUE, false)

    @Bean
    fun notificationsQueue(): Queue = Queue(NOTIFICATIONS_QUEUE, false)

    @Bean
    fun partialUpdatesQueue(): Queue = Queue(PARTIALS_QUEUE, false)

    @Bean
    fun exchange(): TopicExchange = TopicExchange(BIOSTUDIES_EXCHANGE)

    @Bean
    fun logQueueBinding(exchange: TopicExchange): Binding =
        BindingBuilder.bind(logQueue()).to(exchange).with(SUBMISSIONS_ROUTING_KEY)

    @Bean
    fun notificationsQueueBinding(exchange: TopicExchange): Binding =
        BindingBuilder.bind(notificationsQueue()).to(exchange).with(SUBMISSIONS_ROUTING_KEY)

    @Bean
    fun partialUpdatesQueueBinding(exchange: TopicExchange): Binding =
        BindingBuilder.bind(partialUpdatesQueue()).to(exchange).with(SUBMISSIONS_ROUTING_KEY)

    @Bean
    fun jsonMessageConverter(): MessageConverter = Jackson2JsonMessageConverter()

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}
