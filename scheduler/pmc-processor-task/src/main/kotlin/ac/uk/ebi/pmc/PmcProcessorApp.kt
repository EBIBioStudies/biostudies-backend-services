package ac.uk.ebi.pmc

import ac.uk.ebi.pmc.load.PmcLoader
import ac.uk.ebi.pmc.process.PmcSubmissionProcessor
import ac.uk.ebi.pmc.submit.PmcSubmissionSubmitter
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.commons.http.slack.ErrorNotification
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.ReportNotification
import org.springframework.beans.factory.getBean
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import java.io.File

private const val SYSTEM = "PMC_PROCESSOR"

@SpringBootApplication
class PmcProcessorApp {

    @Bean
    @ConfigurationProperties("app.data")
    fun properties() = PmcImporterProperties()

    @Bean
    fun taskExecutor(properties: PmcImporterProperties, notificationSender: NotificationsSender) =
        TaskExecutor(properties, notificationSender)
}

class TaskExecutor(
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

        try {
            when (mode) {
                PmcMode.LOAD -> context.getBean<PmcLoader>().loadFolder(File(properties.path))
                PmcMode.PROCESS -> context.getBean<PmcSubmissionProcessor>().processSubmissions()
                PmcMode.SUBMIT -> context.getBean<PmcSubmissionSubmitter>().submit()
            }
        } catch (exception: RuntimeException) {
            notificationSender.sent(ErrorNotification(SYSTEM, "PMC $mode process", "Process Fail", exception.message))
        }

        notificationSender.sent(ReportNotification(SYSTEM, "PMC $mode process", "Process was completed successfully"))
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PmcProcessorApp>(*args)
}
