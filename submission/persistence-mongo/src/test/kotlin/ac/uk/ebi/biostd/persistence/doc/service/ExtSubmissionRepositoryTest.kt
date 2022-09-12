package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocFileListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSectionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtFileListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSectionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper.docSubmission
import ac.uk.ebi.biostd.persistence.doc.test.beans.TestConfig
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
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
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig.extSerializationService
import uk.ac.ebi.extended.test.FileListFactory.FILE_PATH
import uk.ac.ebi.extended.test.FileListFactory.defaultFileList
import uk.ac.ebi.extended.test.FireFileFactory.defaultFireFile
import uk.ac.ebi.extended.test.SectionFactory.defaultSection
import uk.ac.ebi.extended.test.SubmissionFactory.ACC_NO
import uk.ac.ebi.extended.test.SubmissionFactory.OWNER
import uk.ac.ebi.extended.test.SubmissionFactory.SUBMITTER
import uk.ac.ebi.extended.test.SubmissionFactory.defaultSubmission
import uk.ac.ebi.serialization.common.FilesResolver
import java.time.Duration
import kotlin.text.Typography.section

@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class, TestConfig::class])
class ExtSubmissionRepositoryTest(
    @Autowired private val filesResolver: FilesResolver,
    @Autowired private val subDataRepository: SubmissionDocDataRepository,
    @Autowired private val draftDocDataRepository: SubmissionDraftDocDataRepository,
    @Autowired private val fileListDocFileRepository: FileListDocFileRepository
) {
    private val extSerializationService = extSerializationService()
    private val toFileListMapper =
        ToExtFileListMapper(fileListDocFileRepository, extSerializationService, filesResolver)
    private val toExtSectionMapper = ToExtSectionMapper(toFileListMapper)
    private val toDocFileListMapper = ToDocFileListMapper(extSerializationService)
    private val toDocSectionMapper = ToDocSectionMapper(toDocFileListMapper)
    private val testInstance = ExtSubmissionRepository(
        subDataRepository,
        draftDocDataRepository,
        fileListDocFileRepository,
        ToExtSubmissionMapper(toExtSectionMapper),
        ToDocSubmissionMapper(toDocSectionMapper)
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

        subDataRepository.save(docSubmission.copy(accNo = ACC_NO, version = 1))
        assertThat(subDataRepository.findAll()).hasSize(1)

        val result = testInstance.saveSubmission(submission)

        assertThat(result.section).isEqualToIgnoringGivenFields(
            section.copy(fileList = defaultFileList(filesUrl = null)),
            "fileList"
        )

        assertThat(subDataRepository.getSubmission(submission.accNo, -1)).isNotNull()
        val savedSubmission = subDataRepository.getSubmission(submission.accNo, 2)
        assertThat(subDataRepository.findAll()).hasSize(2)

        val fileListDocFiles = fileListDocFileRepository.findAll()
        assertThat(fileListDocFiles).hasSize(1)
        val fileListDocFile = fileListDocFiles.first()
        assertThat(fileListDocFile.file).isEqualTo(defaultFireFile().toDocFile())
        assertThat(fileListDocFile.submissionId).isEqualTo(savedSubmission.id)
        assertThat(fileListDocFile.fileListName).isEqualTo(FILE_PATH)
        assertThat(fileListDocFile.index).isEqualTo(0)
        assertThat(fileListDocFile.submissionVersion).isEqualTo(savedSubmission.version)
        assertThat(fileListDocFile.submissionAccNo).isEqualTo(submission.accNo)

        assertThat(draftDocDataRepository.findAll()).hasSize(0)
    }

    @Test
    fun deleteSubmissionDrafts() {
        val submission = defaultSubmission(version = 3)

        draftDocDataRepository.saveDraft("someone", "draftKey", "content")
        draftDocDataRepository.saveDraft(OWNER, ACC_NO, "content")
        draftDocDataRepository.saveDraft(SUBMITTER, ACC_NO, "content")
        assertThat(draftDocDataRepository.findAll()).hasSize(3)

        testInstance.deleteSubmissionDrafts(submission, "draftKey")

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
