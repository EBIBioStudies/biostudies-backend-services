package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper.docSubmission
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSING
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import uk.ac.ebi.extended.test.FileListFactory.FILE_PATH
import uk.ac.ebi.extended.test.FileListFactory.defaultFileList
import uk.ac.ebi.extended.test.FireFileFactory.defaultFireFile
import uk.ac.ebi.extended.test.SectionFactory.defaultSection
import uk.ac.ebi.extended.test.SubmissionFactory.ACC_NO
import uk.ac.ebi.extended.test.SubmissionFactory.OWNER
import uk.ac.ebi.extended.test.SubmissionFactory.SUBMITTER
import uk.ac.ebi.extended.test.SubmissionFactory.defaultSubmission
import java.time.Duration

@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class ExtSubmissionRepositoryTest(
    @Autowired private val subDataRepository: SubmissionDocDataRepository,
    @Autowired private val draftDocDataRepository: SubmissionDraftDocDataRepository,
    @Autowired private val fileListDocFileRepository: FileListDocFileRepository,
) {
    private val testInstance = ExtSubmissionRepository(
        subDataRepository,
        draftDocDataRepository,
        fileListDocFileRepository,
        ToExtSubmissionMapper()
    )

    @BeforeEach
    fun beforeEach() {
        subDataRepository.deleteAll()
        draftDocDataRepository.deleteAll()
        fileListDocFileRepository.deleteAll()
    }

    @Test
    fun saveSubmission() {
        val section = defaultSection(fileList = defaultFileList(files = listOf(defaultFireFile())))
        val submission = defaultSubmission(section = section, version = 2)

        subDataRepository.save(docSubmission.copy(accNo = ACC_NO, status = PROCESSED, version = 1))
        assertThat(subDataRepository.findAll()).hasSize(1)

        draftDocDataRepository.saveDraft("someone", "draftKey", "content")
        draftDocDataRepository.saveDraft(OWNER, ACC_NO, "content")
        draftDocDataRepository.saveDraft(SUBMITTER, ACC_NO, "content")
        assertThat(draftDocDataRepository.findAll()).hasSize(3)

        val result = testInstance.saveSubmission(submission, draftKey = "draftKey")

        assertThat(result.section).isEqualTo(section.copy(fileList = defaultFileList(filesUrl = null)))
        assertThat(result.status).isEqualTo(PROCESSING)

        assertThat(subDataRepository.getSubmission(submission.accNo, -1)).isNotNull()
        val savedSubmission = subDataRepository.getSubmission(submission.accNo, 2)
        assertThat(savedSubmission.status).isEqualTo(PROCESSED)
        assertThat(subDataRepository.findAll()).hasSize(2)

        val fileListDocFiles = fileListDocFileRepository.findAll()
        assertThat(fileListDocFiles).hasSize(1)
        val fileListDocFile = fileListDocFiles.first()
        assertThat(fileListDocFile.file).isEqualTo(defaultFireFile().toDocFile())
        assertThat(fileListDocFile.submissionId).isEqualTo(savedSubmission.id)
        assertThat(fileListDocFile.fileListName).isEqualTo(FILE_PATH.substringAfterLast("/"))
        assertThat(fileListDocFile.index).isEqualTo(0)
        assertThat(fileListDocFile.submissionVersion).isEqualTo(savedSubmission.version)
        assertThat(fileListDocFile.submissionAccNo).isEqualTo(submission.accNo)

        assertThat(draftDocDataRepository.findAll()).hasSize(0)
    }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
            .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
