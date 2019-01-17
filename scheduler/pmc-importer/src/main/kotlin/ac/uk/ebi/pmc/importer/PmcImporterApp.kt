package ac.uk.ebi.pmc.importer

import ac.uk.ebi.pmc.importer.import.BatchPmcImporter
import ac.uk.ebi.scheduler.properties.ImportMode
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.io.File

@SpringBootApplication
class PmcImporterApp {

    @Bean
    fun taskExecutor(properties: PmcImporterProperties, importer: BatchPmcImporter) = TaskExecutor(properties, importer)
}

class TaskExecutor(
    private val properties: PmcImporterProperties,
    private val batchImporter: BatchPmcImporter
) : CommandLineRunner {

    override fun run(args: Array<String>) {
        when (properties.mode) {
            ImportMode.FILE -> batchImporter.importFile(File(properties.path))
            ImportMode.GZ_FILE -> batchImporter.importGzipFile(File(properties.path))
        }
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PmcImporterApp>(*args)
}