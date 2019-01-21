package ac.uk.ebi.pmc

import ac.uk.ebi.pmc.submit.PmcBatchSubmitter
import ac.uk.ebi.pmc.import.PmcBatchImporter
import ac.uk.ebi.scheduler.properties.ImportMode
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.io.File

@SpringBootApplication
class PmcProcessorApp {
    @Bean
    fun taskExecutor(properties: PmcImporterProperties, importer: PmcBatchImporter, submitter: PmcBatchSubmitter) =
        TaskExecutor(properties, importer, submitter)
}

class TaskExecutor(
    private val properties: PmcImporterProperties,
    private val batchImporter: PmcBatchImporter,
    private val pmcBatchSubmitter: PmcBatchSubmitter
) : CommandLineRunner {

    override fun run(args: Array<String>) {
        when (properties.mode) {
            ImportMode.SUBMIT -> pmcBatchSubmitter.submit()
            ImportMode.FILE -> batchImporter.importFile(File(properties.path))
            ImportMode.GZ_FILE -> batchImporter.importGzipFile(File(properties.path))
        }
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PmcProcessorApp>(*args)
}
