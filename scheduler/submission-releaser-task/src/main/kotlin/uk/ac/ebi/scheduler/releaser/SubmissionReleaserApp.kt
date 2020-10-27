package uk.ac.ebi.scheduler.releaser

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import uk.ac.ebi.scheduler.releaser.service.SubmissionReleaserService

@SpringBootApplication
@EnableConfigurationProperties
class SubmissionReleaserApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SubmissionReleaserApp>(*args)
}

class SubmissionReleaserExecutor(
    private val submissionReleaserService: SubmissionReleaserService
) : CommandLineRunner, ApplicationContextAware {
    private lateinit var context: ApplicationContext

    override fun run(vararg args: String?) {
        submissionReleaserService.releaseDailySubmissions()
        submissionReleaserService.notifySubmissionReleases()
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }
}
