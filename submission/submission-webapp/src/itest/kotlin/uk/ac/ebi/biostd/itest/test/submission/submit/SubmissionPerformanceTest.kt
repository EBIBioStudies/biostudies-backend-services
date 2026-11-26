package uk.ac.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
import uk.ac.ebi.biostd.asserts.submitAndWait
import uk.ac.ebi.biostd.client.integration.commons.SubmissionFormat
import uk.ac.ebi.biostd.client.integration.web.BioWebClient
import uk.ac.ebi.biostd.dsl.tsv.line
import uk.ac.ebi.biostd.dsl.tsv.tsv
import uk.ac.ebi.biostd.io.ext.createFile
import uk.ac.ebi.biostd.itest.common.SecurityTestService
import uk.ac.ebi.biostd.itest.entities.SuperUser
import uk.ac.ebi.biostd.itest.itest.ITestListener.Companion.FIXED_DELAY_ENV
import uk.ac.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import uk.ac.ebi.biostd.itest.itest.getWebClient
import uk.ac.ebi.biostd.util.date.toStringDate
import java.time.OffsetDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionPerformanceTest(
    @param:Autowired val securityTestService: SecurityTestService,
    @param:LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    @EnabledIfEnvironmentVariable(named = FIXED_DELAY_ENV, matches = "\\d+")
    @EnabledIfSystemProperty(named = "enableFire", matches = "true")
    fun `With many files`() =
        runTest {
            val files = 100
            val delay = System.getenv(FIXED_DELAY_ENV).toLong()

            val subFiles = (1..files).map { tempFolder.createFile("$it.txt") }
            webClient.uploadFiles(subFiles)

            val fileList =
                tempFolder.createFile(
                    "FileList.tsv",
                    tsv {
                        line("Files")
                        subFiles.forEach { line(it.name) }
                    }.toString(),
                )
            webClient.uploadFile(fileList)

            val submission =
                tsv {
                    line("Submission", "SPER-1")
                    line("Title", "Performance Submission")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Study")
                    line("File List", "FileList.tsv")
                    line()
                }.toString()

            val executionTime = measureTime { webClient.submitAndWait(submission, SubmissionFormat.TSV) }

            // Execution time is bounded by 9 times the delay on each Fire operation
            val expectedTime = (9.0 * (files * delay)).toLong()
            assertThat(executionTime.inWholeMilliseconds).isLessThan(expectedTime)
        }
}
