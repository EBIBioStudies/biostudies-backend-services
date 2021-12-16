package uk.ac.ebi.scheculer.pmc.exporter

import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import uk.ac.ebi.scheculer.pmc.exporter.service.PmcExporterService

@SpringBootApplication
@EnableConfigurationProperties
class PmcExporterApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PmcExporterApp>(*args)
}

class PmcExporterExecutor(
    private val pmcExporterService: PmcExporterService
) : CommandLineRunner, ApplicationContextAware {
    private lateinit var context: ApplicationContext

    override fun run(vararg args: String?) {
        runBlocking { pmcExporterService.exportPmcLinks() }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }
}
