package uk.ac.ebi.scheduler.migrator

import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import uk.ac.ebi.scheduler.migrator.service.SubmissionMigratorService

@SpringBootApplication
@EnableMongoRepositories
@EnableConfigurationProperties
class SubmissionMigratorApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SubmissionMigratorApp>(*args)
}

class SubmissionMigratorExecutor(
    private val submissionMigratorService: SubmissionMigratorService,
) : CommandLineRunner, ApplicationContextAware {
    private lateinit var context: ApplicationContext

    override fun run(vararg args: String?) {
        runBlocking { submissionMigratorService.migrateSubmissions() }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }
}
