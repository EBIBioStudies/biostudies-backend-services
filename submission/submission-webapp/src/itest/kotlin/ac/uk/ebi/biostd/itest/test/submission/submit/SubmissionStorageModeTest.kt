package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.fireSubmissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.nfsSubmissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.ext.asFileList
import ebi.ac.uk.io.ext.createFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(PersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionStorageModeTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val sequenceRepository: SequenceDataRepository,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)

        sequenceRepository.save(DbSequence("S-STR-MODE"))
    }

    @Test
    fun `Fire to Nfs`() {
        val submission = tsv {
            line("Submission", "S-STR-MODE-1")
            line("Title", "Storage mode submission")
            line()

            line("Study")
            line("Type", "Experiment")
            line()

            line("File", "one_file.txt")
            line("Type", "test")
            line()
        }.toString()

        webClient.uploadFiles(listOf(tempFolder.createFile("one_file.txt", "content")))

        assertThat(webClient.submitSingle(submission, TSV, FIRE)).isSuccessful()
        val fireSub = submissionRepository.getExtByAccNo("S-STR-MODE-1")
        assertThat(fireSub.storageMode).isEqualTo(FIRE)
        assertThat(fireSub.version).isEqualTo(1)
        assertThat(fireSub.section.files.first()).hasLeftValueSatisfying { assertFile<FireFile>(it) }

        assertThat(webClient.submitSingle(submission, TSV, NFS)).isSuccessful()
        val nfsSub = submissionRepository.getExtByAccNo("S-STR-MODE-1")
        assertThat(nfsSub.version).isEqualTo(2)
        assertThat(nfsSub.storageMode).isEqualTo(NFS)
        assertThat(nfsSub.section.files.first()).hasLeftValueSatisfying { assertFile<NfsFile>(it) }
        assertThat(fireSubmissionPath.resolve(fireSub.relPath).asFileList().filter { it.isFile }).isEmpty()
    }

    @Test
    fun `Nfs to Fire`() {
        val submission = tsv {
            line("Submission", "S-STR-MODE-2")
            line("Title", "Storage mode submission")
            line()

            line("Study")
            line("Type", "Experiment")
            line()

            line("File", "one_file.txt")
            line("Type", "test")
            line()
        }.toString()

        webClient.uploadFiles(listOf(tempFolder.createFile("one_file.txt", "content")))

        assertThat(webClient.submitSingle(submission, TSV, NFS)).isSuccessful()
        val nfsSub = submissionRepository.getExtByAccNo("S-STR-MODE-2")
        assertThat(nfsSub.storageMode).isEqualTo(NFS)
        assertThat(nfsSub.version).isEqualTo(1)
        assertThat(nfsSub.section.files.first()).hasLeftValueSatisfying { assertFile<FireFile>(it) }

        assertThat(webClient.submitSingle(submission, TSV, NFS)).isSuccessful()
        val fireSub = submissionRepository.getExtByAccNo("S-STR-MODE-2")
        assertThat(fireSub.version).isEqualTo(2)
        assertThat(fireSub.storageMode).isEqualTo(FIRE)
        assertThat(fireSub.section.files.first()).hasLeftValueSatisfying { assertFile<NfsFile>(it) }
        assertThat(nfsSubmissionPath.resolve(nfsSub.relPath).asFileList().filter { it.isFile }).isEmpty()
    }

    private inline fun <reified T> assertFile(file: ExtFile): T {
        assertThat(file.fileName).isEqualTo("one_file.txt")
        assertThat(file.attributes.first()).isEqualTo(ExtAttribute("Type", "test"))
        assertThat(file).isInstanceOf(T::class.java)
        return file as T
    }

}
