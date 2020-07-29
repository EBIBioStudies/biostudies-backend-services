package ac.uk.ebi.pmc

import ac.uk.ebi.pmc.load.PmcLoader
import ac.uk.ebi.pmc.process.PmcSubmissionProcessor
import ac.uk.ebi.pmc.submit.PmcSubmissionSubmitter
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import arrow.core.Try
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import mu.KotlinLogging
import org.springframework.beans.factory.getBean
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

private const val SYSTEM = "PMC_PROCESSOR"
private val logger = KotlinLogging.logger {}

@Configuration
class MainConfig {
    @Bean
    @ConfigurationProperties("app.data")
    fun properties() =
        PmcImporterProperties()

    @Bean
    fun pmcTaskExecutor(properties: PmcImporterProperties, notificationSender: NotificationsSender) =
        PmcTaskExecutor(properties, notificationSender)
}

@SpringBootApplication
class PmcProcessorApp

class PmcTaskExecutor(
    private val properties: PmcImporterProperties,
    private val notificationSender: NotificationsSender
) : CommandLineRunner, ApplicationContextAware {
    private lateinit var context: ApplicationContext

    override fun setApplicationContext(context: ApplicationContext) {
        this.context = context
    }

    /**
     * Run the application by validating the mode, note that beans are not directly injected to avoid loaded when they
     * are not needed.
     */
    @Suppress("TooGenericExceptionCaught")
    override fun run(args: Array<String>) {
        val mode = properties.mode
        Try {
            when (mode) {
                PmcMode.LOAD -> context.getBean<PmcLoader>().loadFolder(File(properties.path))
                PmcMode.PROCESS -> context.getBean<PmcSubmissionProcessor>().processSubmissions()
                PmcMode.SUBMIT -> context.getBean<PmcSubmissionSubmitter>().submit()
            }
        }.fold({
            logger.error(it) { "Error executing pmc task, mode = ${properties.mode} " }
            notificationSender.sent(Alert(SYSTEM, mode.description, "Error executing process", it.message))
        }, {
            notificationSender.sent(Report(SYSTEM, mode.description, "Process was completed successfully"))
        })
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PmcProcessorApp>(*args)
}
