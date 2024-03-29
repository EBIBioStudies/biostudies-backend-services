package uk.ac.ebi.scheduler.stats

import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import uk.ac.ebi.scheduler.stats.service.StatsReporterService
import kotlin.system.exitProcess

@SpringBootApplication
@EnableMongoRepositories
@EnableConfigurationProperties
class StatsReporterTask

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<StatsReporterTask>(*args)
}

class StatsReporterExecutor(
    private val statsReporterService: StatsReporterService,
) : CommandLineRunner, ApplicationContextAware {
    private lateinit var context: ApplicationContext

    override fun run(vararg args: String?) {
        runBlocking { statsReporterService.reportStats() }
        exitProcess(0)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }
}
