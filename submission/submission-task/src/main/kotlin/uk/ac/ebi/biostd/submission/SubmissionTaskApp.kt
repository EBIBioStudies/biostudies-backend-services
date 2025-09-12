package uk.ac.ebi.biostd.submission

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.common.properties.Mode.HANDLE_REQUEST
import ac.uk.ebi.biostd.common.properties.Mode.POST_PROCESS_ALL
import ac.uk.ebi.biostd.common.properties.TaskProperties
import ac.uk.ebi.biostd.submission.config.SubmissionConfig
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionPostProcessingService
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@SpringBootApplication
@EnableConfigurationProperties(value = [ApplicationProperties::class, TaskProperties::class])
@Import(value = [SubmissionConfig::class])
class SubmissionTaskApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplicationBuilder(SubmissionTaskApp::class.java)
        .web(WebApplicationType.NONE)
        .run(*args)
}

private val logger = KotlinLogging.logger {}

@Component
class Execute(
    private val properties: TaskProperties,
    private val context: ConfigurableApplicationContext,
    private val submissionSubmitter: ExtSubmissionSubmitter,
    private val submissionPostProcessingService: SubmissionPostProcessingService,
) : CommandLineRunner {
    override fun run(vararg args: String?): Nothing {
        logger.info { "Starting submission task command line runner." }
        runBlocking {
            when (properties.taskMode) {
                HANDLE_REQUEST -> handleRequest()
                POST_PROCESS_ALL -> postProcessAll()
            }
            exitProcess(SpringApplication.exit(context))
        }
    }

    private suspend fun handleRequest() {
        logger.info { "Handling submission requests --------------------------------------------------" }
        properties.submissions.forEach { runProcess(it.accNo, it.version) }
        logger.info { "Completed handling submission requests ----------------------------------------" }
    }

    private suspend fun postProcessAll() {
        logger.info { "Started post processing all submissions ----------------------------------------" }
        submissionPostProcessingService.postProcessAll()
        logger.info { "Finished post processing all submissions ---------------------------------------" }
    }

    private suspend fun runProcess(
        accNo: String,
        version: Int,
    ) {
        runCatching {
            logger.info { "Running ${properties.taskMode} for submission '$accNo', version : '$version'" }
            submissionSubmitter.handleRequest(accNo, version)
            logger.info { "Command line ${properties.taskMode} completed for submission '$accNo', version : '$version'" }
        }.onFailure {
            logger.info(it) { "Failed to process submission '$accNo', version : '$version'" }
        }
    }
}
