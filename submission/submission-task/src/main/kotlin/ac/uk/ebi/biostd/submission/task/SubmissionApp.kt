package ac.uk.ebi.biostd.submission.task;

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.common.properties.TaskProperties
import ac.uk.ebi.biostd.submission.config.SubmissionConfig
import ac.uk.ebi.biostd.submission.domain.request.SubmissionStagesHandler
import ebi.ac.uk.extended.events.RequestCheckedReleased
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestFilesCopied
import ebi.ac.uk.extended.events.RequestFinalized
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.events.RequestPersisted
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

@SpringBootApplication
@EnableConfigurationProperties(value = [ApplicationProperties::class, TaskProperties::class])
@Import(value = [SubmissionConfig::class])
public class SubmissionApp {
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplicationBuilder(SubmissionApp::class.java)
        .web(WebApplicationType.NONE)
        .run(*args)
}

private val logger = KotlinLogging.logger {}

@Component
class Execute(
    private val submissionStagesHandler: SubmissionStagesHandler,
    private val properties: TaskProperties,
    private val context: ConfigurableApplicationContext,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val accNo = properties.accNo
        val version = properties.version
        logger.info { "Running ${properties.taskMode} for submission '$accNo', version : '$version'" }
        when (properties.taskMode) {
            Mode.INDEX -> submissionStagesHandler.indexRequest(RequestCreated(accNo, version))
            Mode.LOAD -> submissionStagesHandler.loadRequest(RequestIndexed(accNo, version))
            Mode.CLEAN -> submissionStagesHandler.cleanRequest(RequestLoaded(accNo, version))
            Mode.COPY -> submissionStagesHandler.copyRequestFiles(RequestCleaned(accNo, version))
            Mode.CHECK_RELEASED -> submissionStagesHandler.checkReleased(RequestFilesCopied(accNo, version))
            Mode.SAVE -> submissionStagesHandler.saveSubmission(RequestCheckedReleased(accNo, version))
            Mode.FINALIZE -> submissionStagesHandler.finalizeRequest(RequestPersisted(accNo, version))
            Mode.CALC_STATS -> submissionStagesHandler.calculateStats(RequestFinalized(accNo, version))
        }
        logger.info { "Command line ${properties.taskMode} completed for submission '$accNo', version : '$version'" }
        System.exit(SpringApplication.exit(context));
    }
}
