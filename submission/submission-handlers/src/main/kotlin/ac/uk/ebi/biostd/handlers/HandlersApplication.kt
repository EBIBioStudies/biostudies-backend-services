package ac.uk.ebi.biostd.handlers

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets.UTF_8

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<HandlersApplication>(*args)
}

@SpringBootApplication
@EnableRabbit
class HandlersApplication {
    @Bean
    fun jsonMessageConverter(): MessageConverter = Jackson2JsonMessageConverter()

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate().apply {
        messageConverters.add(0, StringHttpMessageConverter(UTF_8))
    }
}
