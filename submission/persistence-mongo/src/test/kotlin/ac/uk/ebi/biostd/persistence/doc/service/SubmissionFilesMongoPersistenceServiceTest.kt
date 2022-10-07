package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionFilesRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.doc.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import arrow.core.Either
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.NfsFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@ExtendWith(MockKExtension::class, SpringExtension::class, TemporaryFolderExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionFilesMongoPersistenceServiceTest(
    private val tempFolder: TemporaryFolder,
    @Autowired private val submissionRepo: SubmissionDocDataRepository,
    @Autowired private val fileListDocFileRepository: FileListDocFileRepository,
    @Autowired private val submissionFilesRepository: SubmissionFilesRepository,
) {
    private val testInstance =
        SubmissionFilesMongoPersistenceService(
            fileListDocFileRepository,
            submissionFilesRepository,
        )

    @AfterEach
    fun afterEach() {
        submissionRepo.deleteAll()
        fileListDocFileRepository.deleteAll()
    }

    @Nested
    inner class GetReferencedFiles {
        private val fileReference = ObjectId()
        private val referencedFile = tempFolder.createFile("referenced.txt")
        private val fileListFile = FileListDocFile(
            fileReference,
            testDocSubmission.id,
            NfsDocFile(
                "referenced.txt",
                "referenced.txt",
                "referenced.txt",
                referencedFile.absolutePath,
                listOf(),
                "test-md5",
                1,
                "file"
            ),
            fileListName = "test-file-list",
            index = 1,
            submissionVersion = testDocSubmission.version,
            submissionAccNo = testDocSubmission.accNo
        )
        private val fileList = DocFileList("test-file-list")
        private val submission =
            testDocSubmission.copy(section = DocSection(id = ObjectId(), type = "Study", fileList = fileList))

        @BeforeEach
        fun beforeEach() {
            submissionRepo.save(submission)
            fileListDocFileRepository.save(fileListFile)
        }

        @Test
        fun `get referenced files`() {
            val files = testInstance.getReferencedFiles(SUB_ACC_NO, "test-file-list")
            assertThat(files).hasSize(1)
            assertThat((files.first() as NfsFile).file).isEqualTo(referencedFile)
        }

        @Test
        fun `get referenced files for inner section`() {
            val innerSection = DocSection(id = ObjectId(), type = "Experiment", fileList = fileList)
            val rootSection = DocSection(id = ObjectId(), type = "Study", sections = listOf(Either.left(innerSection)))
            val innerSectionSubmission = testDocSubmission.copy(accNo = "S-REF1", section = rootSection)

            submissionRepo.save(innerSectionSubmission)
            fileListDocFileRepository.save(fileListFile.copy(submissionAccNo = innerSectionSubmission.accNo))

            val files = testInstance.getReferencedFiles("S-REF1", "test-file-list")
            assertThat(files).hasSize(1)
            assertThat((files.first() as NfsFile).file).isEqualTo(referencedFile)
        }

        @Test
        fun `non existing referenced files`() {
            assertThat(testInstance.getReferencedFiles(SUB_ACC_NO, "non-existing-fileListName")).hasSize(0)
        }
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
