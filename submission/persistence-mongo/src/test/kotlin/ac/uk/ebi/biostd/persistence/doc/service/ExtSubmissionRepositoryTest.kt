package ac.uk.ebi.biostd.persistence.doc.service

import DefaultFileList.Companion.defaultFileList
import DefaultFireFile.Companion.defaultFireFile
import DefaultSection.Companion.defaultSection
import DefaultSubmission.Companion.ACC_NO
import DefaultSubmission.Companion.OWNER
import DefaultSubmission.Companion.SUBMITTER
import DefaultSubmission.Companion.defaultSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper.docSubmission
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSING
import io.mockk.slot
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
        val submission = defaultSubmission(section = section)

        subDataRepository.save(docSubmission.copy(accNo = ACC_NO, status = PROCESSED, version = 1))
        assertThat(subDataRepository.findAll()).hasSize(1)

        draftDocDataRepository.saveDraft("someone", "draftKey", "content")
        draftDocDataRepository.saveDraft(OWNER, ACC_NO, "content")
        draftDocDataRepository.saveDraft(SUBMITTER, ACC_NO, "content")
        assertThat(draftDocDataRepository.findAll()).hasSize(3)

        val result = testInstance.saveSubmission(submission, draftKey = "draftKey")

        assertThat(result.section).isEqualTo(section.copy(fileList = defaultFileList(filesUrl = null)))
        assertThat(result.status).isEqualTo(PROCESSING)

        val savedSubmissions = subDataRepository.getSubmissions(SubmissionFilter(submission.accNo))
        assertThat(savedSubmissions).hasSize(1)
        val savedSubmission = savedSubmissions.first()
        assertThat(savedSubmission.status).isEqualTo(PROCESSED)
        assertThat(subDataRepository.findAll()).hasSize(2)

        val fileListDocFiles = fileListDocFileRepository.findAll()
        assertThat(fileListDocFiles).hasSize(1)
        val fileListDocFile = fileListDocFiles.first()
        assertThat(fileListDocFile.submissionId).isEqualTo(savedSubmission.id)
        assertThat(fileListDocFile.file).isEqualTo(defaultFireFile().toDocFile())

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
            register.add("app.persistence.enableMongo") { "true" }
        }
    }
}
