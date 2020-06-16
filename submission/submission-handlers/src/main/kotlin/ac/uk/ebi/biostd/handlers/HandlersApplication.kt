package ac.uk.ebi.biostd.handlers

import ac.uk.ebi.biostd.handlers.listeners.LogSubmissionReceiver
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val BIOSTUDIES_EXCHANGE = "biostudies-exchange"
const val ROUTING_KEY = "bio.submission.published"

const val LOG_QUEUE = "submission-submitted-log-queue"
const val PARTIALS_QUEUE = "submission-submitted-updates-queue"

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<HandlersApplication>(*args)
}

@SpringBootApplication
@EnableRabbit
class HandlersApplication {

    @Bean
    fun logQueue(): Queue {
        return Queue(LOG_QUEUE, false)
    }

    @Bean
    fun ftpQueue(): Queue {
        return Queue(PARTIALS_QUEUE, false)
    }

    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange(BIOSTUDIES_EXCHANGE)
    }

    @Bean
    fun logQueueBinding(exchange: TopicExchange): Binding {
        return BindingBuilder.bind(logQueue()).to(exchange).with(ROUTING_KEY)
    }

    @Bean
    fun ftpQueueBinding(exchange: TopicExchange): Binding {
        return BindingBuilder.bind(ftpQueue()).to(exchange).with(ROUTING_KEY)
    }

    @Bean
    fun jsonMessageConverter(): MessageConverter {
        return Jackson2JsonMessageConverter()
    }
}

@Configuration
class Listeners {

    @Bean
    @ConfigurationProperties("app")
    fun appProperties() = AppProperties()

    @Bean
    fun logListener() = LogSubmissionReceiver()
}
