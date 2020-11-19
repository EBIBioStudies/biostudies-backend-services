package uk.ac.ebi.scheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SchedulerApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SchedulerApp>(*args)
}
