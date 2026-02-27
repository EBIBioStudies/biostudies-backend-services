package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV_EXTENSION
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.coroutines.waitUntil
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Durations.FIVE_SECONDS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MultipartAsyncTest(
    @param:Autowired private val securityTestService: SecurityTestService,
    @param:Autowired val submissionRepository: SubmissionPersistenceQueryService,
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
    fun multipartAsync() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "SMulti-001")
                    line("Title", "Submission")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Study")
                    line()

                    line("File", "file.txt")
                }.toString()

            val submission2 =
                tsv {
                    line("Submission", "SMulti-002")
                    line("Title", "Submission")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Study")
                    line()

                    line("Files")
                    line("file2.txt")
                    line("file3.txt")
                }.toString()

            val file = tempFolder.createFile("file.txt", "content")
            val file2 = tempFolder.createFile("file2.txt", "content")
            val file3 = tempFolder.createFile("file3.txt", "content")

            val result =
                webClient.submitMultipartAsync(
                    format = TSV_EXTENSION,
                    submissions = mapOf("SMulti-001" to submission, "SMulti-002" to submission2),
                    files = mapOf("SMulti-001" to listOf(file), "SMulti-002" to listOf(file2, file3)),
                    parameters = SubmitParameters(storageMode = storageMode),
                )

            assertThat(result).hasSize(2)
            val sub1 = result.first()
            assertThat(sub1.accNo).isEqualTo("SMulti-001")
            assertThat(sub1.version).isEqualTo(1)

            val sub2 = result[1]
            assertThat(sub2.accNo).isEqualTo("SMulti-002")
            assertThat(sub2.version).isEqualTo(1)

            waitUntil(timeout = FIVE_SECONDS) { submissionRepository.existByAccNoAndVersion(sub1.accNo, sub1.version) }
            waitUntil(timeout = FIVE_SECONDS) { submissionRepository.existByAccNoAndVersion(sub2.accNo, sub2.version) }

            val storedSub1 = submissionRepository.getExtByAccNoAndVersion(sub1.accNo, sub1.version)
            assertThat(storedSub1.section.files).hasSize(1)
            val storedF1 = storedSub1.section.files.first()
            assertThat(storedF1).hasLeftValueSatisfying { it.fileName == file.name }

            val storedSub2 = submissionRepository.getExtByAccNoAndVersion(sub2.accNo, sub2.version)
            val storedF2 = storedSub2.section.files.first()
            assertThat(storedF2).hasRightValueSatisfying {
                val files = it.files.map { it.fileName }
                assertThat(files).containsExactly(file2.name, file3.name)
            }
        }
}
