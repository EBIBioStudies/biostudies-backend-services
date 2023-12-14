package uk.ac.ebi.biostd.submission

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.common.properties.Mode.CALC_STATS
import ac.uk.ebi.biostd.common.properties.Mode.CHECK_RELEASED
import ac.uk.ebi.biostd.common.properties.Mode.CLEAN
import ac.uk.ebi.biostd.common.properties.Mode.COPY
import ac.uk.ebi.biostd.common.properties.Mode.FINALIZE
import ac.uk.ebi.biostd.common.properties.Mode.INDEX
import ac.uk.ebi.biostd.common.properties.Mode.LOAD
import ac.uk.ebi.biostd.common.properties.Mode.SAVE
import ac.uk.ebi.biostd.common.properties.TaskProperties
import ac.uk.ebi.biostd.submission.config.SubmissionConfig
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.stats.SubmissionStatsService
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
class SubmissionApp

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplicationBuilder(SubmissionApp::class.java)
        .web(WebApplicationType.NONE)
        .run(*args)
}

private val logger = KotlinLogging.logger {}

@Component
class Execute(
    private val properties: TaskProperties,
    private val statsService: SubmissionStatsService,
    private val context: ConfigurableApplicationContext,
    private val submissionSubmitter: ExtSubmissionSubmitter,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val accNo = properties.accNo
        val version = properties.version

        logger.info { "Running ${properties.taskMode} for submission '$accNo', version : '$version'" }
        runBlocking { runProcess(accNo, version) }
        logger.info { "Command line ${properties.taskMode} completed for submission '$accNo', version : '$version'" }

        exitProcess(SpringApplication.exit(context))
    }

    private suspend fun runProcess(accNo: String, version: Int) {
        when (properties.taskMode) {
            INDEX -> submissionSubmitter.indexRequest(accNo, version)
            LOAD -> submissionSubmitter.loadRequest(accNo, version)
            CLEAN -> submissionSubmitter.cleanRequest(accNo, version)
            COPY -> submissionSubmitter.processRequest(accNo, version)
            CHECK_RELEASED -> submissionSubmitter.checkReleased(accNo, version)
            SAVE -> submissionSubmitter.saveRequest(accNo, version)
            FINALIZE -> submissionSubmitter.finalizeRequest(accNo, version)
            CALC_STATS -> statsService.calculateSubFilesSize(accNo)
        }
    }
}
