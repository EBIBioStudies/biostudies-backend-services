package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.FilePersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.commons.ExtendedUpdate
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ResubmissionApiTest(
    @Autowired val mongoTemplate: MongoTemplate,
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
    fun `5-1 resubmit existing submission`() = runTest {
        val submission = tsv {
            line("Submission", "S-RSTST1")
            line("Title", "Simple Submission With Files")
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
        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val submitted = submissionRepository.getExtByAccNo("S-RSTST1")
        assertThat(submitted.version).isEqualTo(1)
        assertThat(File("$submissionPath/${submitted.relPath}/Files/file section.doc")).hasContent("doc content")
        assertThat(File("$submissionPath/${submitted.relPath}/Files/fileSubSection.txt")).hasContent("content")
        assertThat(File("$submissionPath/${submitted.relPath}/Files/a/fileFileList.pdf")).hasContent("pdf content")

        val changedFile = tempFolder.resolve("fileSubSection.txt").apply { writeText("newContent") }
        webClient.uploadFiles(listOf(changedFile))
        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val resubmitted = submissionRepository.getExtByAccNo("S-RSTST1")
        assertThat(resubmitted.version).isEqualTo(2)
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/file section.doc")).exists()
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/fileSubSection.txt")).hasContent("newContent")
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/a/fileFileList.pdf")).exists()
    }

    @Test
    fun `5-2 resubmit existing submission with the same files`() = runTest {
        val submission = tsv {
            line("Submission", "S-RSTST2")
            line("Title", "Simple Submission With Files 2")
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
                tempFolder.createFile("file section.doc"),
            )
        )
        webClient.uploadFiles(listOf(tempFolder.createFile("fileFileList.pdf")), "a")
        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        val submitted = submissionRepository.getExtByAccNo("S-RSTST2")
        assertThat(submitted.version).isEqualTo(1)
        assertThat(File("$submissionPath/${submitted.relPath}/Files/file section.doc")).exists()
        assertThat(File("$submissionPath/${submitted.relPath}/Files/fileSubSection.txt")).exists()
        assertThat(File("$submissionPath/${submitted.relPath}/Files/fileSubSection.txt")).hasContent("content")
        assertThat(File("$submissionPath/${submitted.relPath}/Files/a/fileFileList.pdf")).exists()

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
        val resubmitted = submissionRepository.getExtByAccNo("S-RSTST2")
        assertThat(resubmitted.version).isEqualTo(2)
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/file section.doc")).exists()
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/fileSubSection.txt")).exists()
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/fileSubSection.txt")).hasContent("content")
        assertThat(File("$submissionPath/${resubmitted.relPath}/Files/a/fileFileList.pdf")).exists()
    }

    @Test
    fun `5-3 re submit a submission with rootPath`() {
        val rootPath = "The-RootPath"
        val dataFile = "DataFile1.txt"

        val submission = tsv {
            line("Submission", "S-RSTST3")
            line("Title", "Sample Submission")
            line("RootPath", rootPath)
            line()
            line("Study")
            line()
            line("File", "DataFile1.txt")
            line()
        }.toString()

        webClient.uploadFiles(listOf(tempFolder.createFile("DataFile1.txt")), rootPath)
        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

        webClient.deleteFile(dataFile, rootPath)

        assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
    }

    @Test
    fun `5-4 regular user suppresses own submission`() {
        val version1 = tsv {
            line("Submission", "S-RSTST4")
            line("Title", "Public Submission")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()
            line("Study")
            line()
        }.toString()

        val regularUser = RegularUser.email
        val onBehalfClient = SecurityWebClient
            .create("http://localhost:$serverPort")
            .getAuthenticatedClient(SuperUser.email, SuperUser.password, regularUser)

        assertThat(onBehalfClient.submitSingle(version1, TSV)).isSuccessful()

        val version2 = tsv {
            line("Submission", "S-RSTST4")
            line("Title", "Suppressed Submission")
            line("ReleaseDate", "2050-05-22")
            line()
            line("Study")
            line()
        }.toString()

        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { getWebClient(serverPort, RegularUser).submitSingle(version2, TSV) }
            .withMessageContaining("The release date of a public study cannot be changed")
    }

    @Test
    fun `5-5 super user suppresses submission from another user`() {
        val version1 = tsv {
            line("Submission", "S-RSTST5")
            line("Title", "Public Submission")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()
            line("Study")
            line()
        }.toString()

        val onBehalfClient = SecurityWebClient
            .create("http://localhost:$serverPort")
            .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

        assertThat(onBehalfClient.submitSingle(version1, TSV)).isSuccessful()

        val version2 = tsv {
            line("Submission", "S-RSTST5")
            line("Title", "Suppressed Submission")
            line("ReleaseDate", "2050-05-22")
            line()
            line("Study")
            line()
        }.toString()

        assertThat(onBehalfClient.submitSingle(version2, TSV)).isSuccessful()
    }

    @Test
    fun `5-6 add metadata to a public submission`() {
        val version1 = tsv {
            line("Submission", "S-RSTST6")
            line("Title", "Simple submission to be updated")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()
            line("Study")
            line()
            line("File", "5-6.txt")
            line()
        }.toString()

        webClient.uploadFiles(listOf(tempFolder.createFile("5-6.txt")))
        assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()

        mongoTemplate.updateMulti(
            Query(where(SUB_ACC_NO).`in`("S-RSTST6").andOperator(where(SUB_VERSION).gt(0))),
            ExtendedUpdate().set(SUB_RELEASE_TIME, OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, UTC).toInstant()),
            DocSubmission::class.java
        )

        val version2 = tsv {
            line("Submission", "S-RSTST6")
            line("Title", "Simple submission to be updated")
            line("ReleaseDate", "2018-10-10")
            line()
            line("Study")
            line("Type", "Exp")
            line()
            line("File", "5-6.txt")
            line()
            line("Link", "CHEBI::19")
            line()
        }.toString()
        assertThat(webClient.submitSingle(version2, TSV)).isSuccessful()
    }

    @Test
    fun `5-7 change the release date of a public submission`() {
        val version1 = tsv {
            line("Submission", "S-RSTST7")
            line("Title", "Release date change test")
            line("ReleaseDate", OffsetDateTime.now().toStringDate())
            line()
            line("Study")
            line()
        }.toString()

        assertThat(webClient.submitSingle(version1, TSV)).isSuccessful()

        mongoTemplate.updateMulti(
            Query(where(SUB_ACC_NO).`in`("S-RSTST7").andOperator(where(SUB_VERSION).gt(0))),
            ExtendedUpdate().set(SUB_RELEASE_TIME, OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, UTC).toInstant()),
            DocSubmission::class.java
        )

        val version2 = tsv {
            line("Submission", "S-RSTST7")
            line("Title", "Release date change test")
            line("ReleaseDate", "2019-11-20")
            line()
            line("Study")
            line()
        }.toString()

        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { webClient.submitSingle(version2, TSV) }
            .withMessageContaining("The release date of a public study cannot be changed")
    }
}
