package uk.ac.ebi.scheduler.pmc.exporter

import ac.uk.ebi.scheduler.properties.ExporterMode.PMC
import ac.uk.ebi.scheduler.properties.ExporterMode.PMC_VIEW
import ac.uk.ebi.scheduler.properties.ExporterMode.PUBLIC_ONLY
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import uk.ac.ebi.scheduler.pmc.exporter.config.ApplicationProperties
import uk.ac.ebi.scheduler.pmc.exporter.service.ExporterService

@SpringBootApplication
@EnableMongoRepositories
@EnableConfigurationProperties
class ExporterApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<ExporterApp>(*args)
}

class ExporterExecutor(
    private val exporterService: ExporterService,
    private val appProperties: ApplicationProperties,
) : CommandLineRunner,
    ApplicationContextAware {
    private lateinit var context: ApplicationContext

    override fun run(vararg args: String?) {
        when (appProperties.mode) {
            PMC_VIEW -> exporterService.updatePmcView()
            PMC -> exporterService.exportPmc()
            PUBLIC_ONLY -> exporterService.exportPublicOnly()
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }
}
