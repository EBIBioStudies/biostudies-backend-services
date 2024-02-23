package ac.uk.ebi.biostd.handlers

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<HandlersApplication>(*args)
}

@SpringBootApplication
@EnableRabbit
class HandlersApplication
