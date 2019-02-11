package ac.uk.ebi.pmc

import ac.uk.ebi.pmc.load.PmcSubmissionLoader
import ac.uk.ebi.pmc.process.PmcSubmissionProcessor
import ac.uk.ebi.pmc.submit.PmcBatchSubmitter
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.io.File

@SpringBootApplication
class PmcProcessorApp {

    @Bean
    fun taskExecutor(
        properties: PmcImporterProperties,
        importer: PmcSubmissionProcessor,
        submitter: PmcBatchSubmitter,
        pmcSubmissionLoader: PmcSubmissionLoader
    ) = TaskExecutor(properties, importer, submitter, pmcSubmissionLoader)
}

class TaskExecutor(
    private val properties: PmcImporterProperties,
    private val processor: PmcSubmissionProcessor,
    private val submitter: PmcBatchSubmitter,
    private val loader: PmcSubmissionLoader
) : CommandLineRunner {

    override fun run(args: Array<String>) {
        when (properties.mode) {
            PmcMode.LOAD -> loader.loadFolder(File(properties.path))
            PmcMode.PROCESS -> processor.processSubmissions()
            PmcMode.SUBMIT -> submitter.submit()
        }
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PmcProcessorApp>(*args)
}
