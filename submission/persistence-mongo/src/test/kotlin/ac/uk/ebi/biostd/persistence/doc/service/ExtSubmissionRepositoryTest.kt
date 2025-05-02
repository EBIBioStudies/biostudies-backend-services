package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocFileListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocLinkListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSectionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtFileListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtLinkListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSectionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper.docSubmission
import ac.uk.ebi.biostd.persistence.doc.test.beans.TestConfig
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.test.basicExtSubmission
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
import uk.ac.ebi.serialization.common.FilesResolver
import java.time.Duration

@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class, TestConfig::class])
class ExtSubmissionRepositoryTest(
    @Autowired private val filesResolver: FilesResolver,
    @Autowired private val subDataRepository: SubmissionDocDataRepository,
    @Autowired private val fileListDocFileRepository: FileListDocFileDocDataRepository,
) {
    private val extSerializationService = extSerializationService()
    private val toFileListMapper =
        ToExtFileListMapper(fileListDocFileRepository, extSerializationService, filesResolver)
    private val toLinkListMapper = ToExtLinkListMapper(filesResolver)
    private val toExtSectionMapper = ToExtSectionMapper(toFileListMapper, toLinkListMapper)
    private val toDocFileListMapper = ToDocFileListMapper(extSerializationService)
    private val toDocLinkListMapper = ToDocLinkListMapper()
    private val toDocSectionMapper = ToDocSectionMapper(toDocFileListMapper, toDocLinkListMapper)
    private val testInstance =
        ExtSubmissionRepository(
            subDataRepository,
            fileListDocFileRepository,
            ToExtSubmissionMapper(toExtSectionMapper),
            ToDocSubmissionMapper(toDocSectionMapper),
        )

    @BeforeEach
    fun beforeEach() =
        runBlocking {
            subDataRepository.deleteAll()
            fileListDocFileRepository.deleteAll()
        }

    @Test
    fun `save submission`() =
        runTest {
            val section = defaultSection(fileList = defaultFileList(files = listOf(defaultFireFile())))
            val submission = basicExtSubmission.copy(section = section)

            val result = testInstance.saveSubmission(submission)

            assertThat(result.section)
                .usingRecursiveComparison()
                .ignoringFields("fileList")
                .isEqualTo(section.copy(fileList = defaultFileList(filesUrl = null)))

            val savedSubmission = subDataRepository.getSubmission(submission.accNo, 1)
            assertThat(subDataRepository.findAll().toList()).hasSize(1)

            val fileListDocFiles = fileListDocFileRepository.findAll().toList()
            assertThat(fileListDocFiles).hasSize(1)
            val fileListDocFile = fileListDocFiles.first()
            assertThat(fileListDocFile.file).isEqualTo(defaultFireFile().toDocFile())
            assertThat(fileListDocFile.submissionId).isEqualTo(savedSubmission.id)
            assertThat(fileListDocFile.fileListName).isEqualTo(FILE_PATH)
            assertThat(fileListDocFile.index).isEqualTo(0)
            assertThat(fileListDocFile.submissionVersion).isEqualTo(savedSubmission.version)
            assertThat(fileListDocFile.submissionAccNo).isEqualTo(submission.accNo)
        }

    @Test
    fun `expire previous versions`() =
        runTest {
            subDataRepository.save(docSubmission.copy(accNo = "S-TEST123", version = 1))
            assertThat(subDataRepository.findAll().toList()).hasSize(1)

            testInstance.expirePreviousVersions("S-TEST123")

            assertThat(subDataRepository.getSubmission("S-TEST123", -1)).isNotNull()
            assertThat(subDataRepository.findAll().toList()).hasSize(1)
        }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
