package uk.ac.ebi.scheduler.exporter

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import uk.ac.ebi.scheduler.exporter.service.ExporterService

@SpringBootApplication
@EnableConfigurationProperties
class ExporterApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<ExporterApp>(*args)
}

class ExporterExecutor(private val exporterService: ExporterService) : CommandLineRunner, ApplicationContextAware {
    private lateinit var context: ApplicationContext

    override fun run(vararg args: String?) = exporterService.exportPublicSubmissions()

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }
}
