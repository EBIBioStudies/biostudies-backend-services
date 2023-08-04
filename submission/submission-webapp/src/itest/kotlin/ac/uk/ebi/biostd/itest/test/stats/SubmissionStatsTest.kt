package ac.uk.ebi.biostd.itest.test.stats

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.FilePersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.util.date.toStringDate
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.awaitility.Durations.TEN_SECONDS
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
class SubmissionStatsTest(
    @Autowired val statsDataService: StatsDataService,
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        securityTestService.ensureUserRegistration(RegularUser)
        webClient = getWebClient(serverPort, SuperUser)
    }

    @Test
    fun `26-1 files size stat calculation on submit`() {
        val version1 = tsv {
            line("Submission", "S-STTS1")
            line("Title", "Stats Registration Test")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()

            line("Study")
            line()

            line("File", "file section.doc")
            line("Type", "test")
            line()
        }.toString()

        val version2 = tsv {
            line("Submission", "S-STTS1")
            line("Title", "Stats Registration Test")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()

            line("Study")
            line("Type", "Experiment")
            line("File List", "file-list.tsv")
            line()

            line("File", "file section.doc")
            line("Type", "test")
            line()

            line("Experiment", "Exp1")
            line("Type", "Subsection")
            line()

            line("File", "fileSubSection.txt")
            line("Type", "Attached")
            line()
        }.toString()

        val fileListContent = tsv {
            line("Files", "Type")
            line("a/fileFileList.pdf", "inner")
            line("a", "folder")
        }.toString()

        webClient.uploadFiles(
            listOf(
                tempFolder.createFile("fileSubSection.txt", "content"),
                tempFolder.createFile("file-list.tsv", fileListContent),
                tempFolder.createFile("file section.doc", "doc content"),
            )
        )
        webClient.uploadFiles(listOf(tempFolder.createFile("fileFileList.pdf", "pdf content")), "a")

        assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()
        await().atMost(TEN_SECONDS).until { statsDataService.findByAccNo("S-STTS1").isNotEmpty() }
        val statVersion1 = statsDataService.findByAccNo("S-STTS1")
        assertThat(statVersion1).hasSize(1)
        assertThat(statVersion1.first().value).isEqualTo(1181L)
        assertThat(statVersion1.first().type).isEqualTo(FILES_SIZE)
        assertThat(statVersion1.first().accNo).isEqualTo("S-STTS1")

        assertThat(webClient.submitSingle(version2, TSV)).isSuccessful()
        await().atMost(TEN_SECONDS).until { statsDataService.findByAccNo("S-STTS1").first().value != 1181L }
        val stats = statsDataService.findByAccNo("S-STTS1")
        assertThat(stats).hasSize(1)
        assertThat(stats.first().value).isEqualTo(3521L)
        assertThat(stats.first().type).isEqualTo(FILES_SIZE)
        assertThat(stats.first().accNo).isEqualTo("S-STTS1")
    }
}
