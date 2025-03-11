package ac.uk.ebi.pmc

import ac.uk.ebi.pmc.load.PmcFileLoader
import ac.uk.ebi.pmc.process.PmcProcessor
import ac.uk.ebi.pmc.submit.PmcSubmitter
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import ac.uk.ebi.scheduler.properties.PmcMode.LOAD
import ac.uk.ebi.scheduler.properties.PmcMode.PROCESS
import ac.uk.ebi.scheduler.properties.PmcMode.SUBMIT
import ac.uk.ebi.scheduler.properties.PmcMode.SUBMIT_SINGLE
import ebi.ac.uk.commons.http.slack.Alert
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import java.io.File

private const val SYSTEM = "PMC_PROCESSOR"
private val logger = KotlinLogging.logger {}

class PmcTaskExecutor(
    private val props: PmcImporterProperties,
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
        runCatching { runProcess(props.mode) }.fold({ onSuccess() }, { onError(it) })
    }

    private fun onError(it: Throwable) =
        runBlocking {
            logger.error(it) { "Error executing pmc task, mode = ${props.mode} " }
            notificationSender.send(
                Alert(
                    SYSTEM,
                    props.mode.description,
                    "Error executing process",
                    it.message,
                ),
            )
        }

    private fun onSuccess() =
        runBlocking {
            notificationSender.send(
                Report(
                    SYSTEM,
                    props.mode.description,
                    "Process was completed successfully",
                ),
            )
        }

    private fun runProcess(mode: PmcMode) {
        when (mode) {
            LOAD -> {
                val folder = File(requireNotNull(props.loadFolder) { "load folder parameter is required" })
                val file = props.loadFile?.let { folder.resolve(it) }
                context.getBean<PmcFileLoader>().loadFile(folder, file)
            }

            PROCESS -> context.getBean<PmcProcessor>().processAll(props.sourceFile)
            SUBMIT -> context.getBean<PmcSubmitter>().submitAll(props.sourceFile, props.limit, props.batchSize)
            SUBMIT_SINGLE -> context.getBean<PmcSubmitter>().submitSingle(requireNotNull(props.submissionId))
        }
    }
}
