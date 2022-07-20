package ac.uk.ebi.pmc

import ac.uk.ebi.pmc.load.PmcFileLoader
import ac.uk.ebi.pmc.process.PmcProcessor
import ac.uk.ebi.pmc.submit.PmcSubmitter
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import mu.KotlinLogging
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import java.io.File

private const val SYSTEM = "PMC_PROCESSOR"
private val logger = KotlinLogging.logger {}

class PmcTaskExecutor(
    private val properties: PmcImporterProperties,
    private val notificationSender: NotificationsSender,
) : ApplicationContextAware {
    private lateinit var context: ApplicationContext

    override fun setApplicationContext(context: ApplicationContext) {
        this.context = context
    }

    /**
     * Run the application by validating the mode, note that beans are not directly injected to avoid loaded when they
     * are not needed.
     */
    @Suppress("TooGenericExceptionCaught")
    fun run() {
        val mode = properties.mode
        runCatching {
            when (mode) {
                PmcMode.LOAD -> context.getBean<PmcFileLoader>().loadFolder(File(properties.loadFolder))
                PmcMode.PROCESS -> context.getBean<PmcProcessor>().processAll()
                PmcMode.SUBMIT -> context.getBean<PmcSubmitter>().submitAll()
            }
        }.fold(
            {
                notificationSender.send(Report(SYSTEM, mode.description, "Process was completed successfully"))
            },
            {
                logger.error(it) { "Error executing pmc task, mode = ${properties.mode} " }
                notificationSender.send(Alert(SYSTEM, mode.description, "Error executing process", it.message))
            },
        )
    }
}
