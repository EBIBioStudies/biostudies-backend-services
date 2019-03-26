package ac.uk.ebi.pmc

import ac.uk.ebi.pmc.load.PmcLoader
import ac.uk.ebi.pmc.process.PmcSubmissionProcessor
import ac.uk.ebi.pmc.submit.PmcSubmissionSubmitter
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import org.springframework.beans.factory.getBean
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import java.io.File

@SpringBootApplication
class PmcProcessorApp {

    @Bean
    @ConfigurationProperties("app.data")
    fun properties() = PmcImporterProperties()

    @Bean
    fun taskExecutor(properties: PmcImporterProperties) = TaskExecutor(properties)
}

class TaskExecutor(
    private val properties: PmcImporterProperties
) : CommandLineRunner, ApplicationContextAware {

    private lateinit var context: ApplicationContext

    override fun setApplicationContext(context: ApplicationContext) {
        this.context = context
    }

    /**
     * Run the application by validating the mode, note that beans are not directly injected to avoid loaded when they
     * are not needed.
     */
    override fun run(args: Array<String>) {
        when (properties.mode) {
            PmcMode.LOAD -> context.getBean<PmcLoader>().loadFolder(File(properties.path))
            PmcMode.PROCESS -> context.getBean<PmcSubmissionProcessor>().processSubmissions()
            PmcMode.SUBMIT -> context.getBean<PmcSubmissionSubmitter>().submit()
        }
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PmcProcessorApp>(*args)
}
