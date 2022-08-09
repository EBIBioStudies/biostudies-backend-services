package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File

@Import(PersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ResubmissionApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)
    }

    @Test
    fun `resubmit existing submission`() {
        val submission = tsv {
            line("Submission", "S-RSTST1")
            line("Title", "Simple Submission With Files")
            line("ReleaseDate", "2020-01-25")
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
    fun `resubmit existing submission with the same files`() {
        val submission = tsv {
            line("Submission", "S-RSTST2")
            line("Title", "Simple Submission With Files 2")
            line("ReleaseDate", "2020-01-25")
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
    fun `re submit a submission with rootPath`() {
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
}
