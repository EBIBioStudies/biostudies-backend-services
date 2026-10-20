package uk.ac.ebi.scheduler.releaser

import ac.uk.ebi.scheduler.properties.ReleaserMode.NOTIFY
import ac.uk.ebi.scheduler.properties.ReleaserMode.RELEASE
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import uk.ac.ebi.scheduler.releaser.config.ApplicationProperties
import uk.ac.ebi.scheduler.releaser.service.SubmissionReleaserService

@SpringBootApplication
@EnableMongoRepositories
@EnableConfigurationProperties
class SubmissionReleaserApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SubmissionReleaserApp>(*args)
}

class SubmissionReleaserExecutor(
    private val applicationProperties: ApplicationProperties,
    private val submissionReleaserService: SubmissionReleaserService,
) : CommandLineRunner,
    ApplicationContextAware {
    private lateinit var context: ApplicationContext

    override fun run(vararg args: String?) {
        runBlocking {
            when (applicationProperties.mode) {
                NOTIFY -> submissionReleaserService.notifySubmissionReleases()
                RELEASE -> submissionReleaserService.releaseDailySubmissions()
            }
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }
}
