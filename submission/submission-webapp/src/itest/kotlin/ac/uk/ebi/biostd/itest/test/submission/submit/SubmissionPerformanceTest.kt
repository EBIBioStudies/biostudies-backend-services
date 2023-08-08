package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.FilePersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.fixedDelayEnv
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionPerformanceTest(
    @Autowired val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)
    }

    @Test
    @EnabledIfEnvironmentVariable(named = fixedDelayEnv, matches = "\\d+")
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `test with many files`() {
        val files = 100
        val delay = System.getenv(fixedDelayEnv).toLong()

        val subFiles = (1..files).map { tempFolder.createFile("${it}.txt") }
        webClient.uploadFiles(subFiles)

        val performanceSubmission = tsv {
            line("Submission", "SPER-1")
            line("Title", "Performance Submission")
            line()

            line("Study")
            line()

            line("Files")
            subFiles.forEach { line(it.name) }
            line()
        }.toString()

        val executionTime = measureTime { webClient.submitSingle(performanceSubmission, SubmissionFormat.TSV) }

        // Execution time is bounded by 9 times the delay on each Fire operation
        val expectedTime = (9.0 * (files * delay)).toLong()
        assertThat(executionTime.inWholeMilliseconds).isLessThan(expectedTime)
    }
}
