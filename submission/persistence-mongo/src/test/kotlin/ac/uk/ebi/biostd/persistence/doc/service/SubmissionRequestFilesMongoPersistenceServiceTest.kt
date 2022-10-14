package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.test.clean
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
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
import java.time.Duration.ofSeconds
import kotlin.streams.toList

@ExtendWith(MockKExtension::class, SpringExtension::class, TemporaryFolderExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionRequestFilesMongoPersistenceServiceTest(
    private val tempFolder: TemporaryFolder,
    @Autowired private val requestFilesRepository: SubmissionRequestFilesRepository,
) {
    private val testInstance = SubmissionRequestFilesMongoPersistenceService(requestFilesRepository)

    @AfterEach
    fun afterEach() {
        tempFolder.clean()
        requestFilesRepository.deleteAll()
    }

    @Nested
    inner class Registration {
        @Test
        fun `register request file`() {
            val extFile = createNfsFile("requested.txt", "Files/requested.txt", tempFolder.createFile("requested.txt"))
            val requestFile = SubmissionRequestFile("S-BSST0", 1, 1, "requested.txt", extFile)

            testInstance.saveSubmissionRequestFile(requestFile)

            val saved = testInstance.getSubmissionRequestFile("requested.txt", "S-BSST0", 1)
            assertThat(saved).isEqualTo(extFile)
        }
    }

    @Nested
    inner class Query {
        private val extFile1 = createNfsFile("file1.txt", "Files/file1.txt", tempFolder.createFile("file1.txt"))
        private val extFile2 = createNfsFile("file2.txt", "Files/file2.txt", tempFolder.createFile("file2.txt"))
        private val extFile3 = createNfsFile("file3.txt", "Files/file3.txt", tempFolder.createFile("file3.txt"))
        private val extFile4 = createNfsFile("file4.txt", "Files/file4.txt", tempFolder.createFile("file4.txt"))

        @BeforeEach
        fun beforeEach() {
            val requestFile1 = SubmissionRequestFile("S-BSST1", 1, 1, "file1.txt", extFile1)
            val requestFile2 = SubmissionRequestFile("S-BSST1", 1, 2, "file2.txt", extFile2, "file-list")
            val requestFile3 = SubmissionRequestFile("S-BSST1", 1, 3, "file3.txt", extFile3, "file-list")
            val requestFile4 = SubmissionRequestFile("S-BSST1", 1, 4, "file4.txt", extFile4)

            testInstance.saveSubmissionRequestFile(requestFile1)
            testInstance.saveSubmissionRequestFile(requestFile2)
            testInstance.saveSubmissionRequestFile(requestFile3)
            testInstance.saveSubmissionRequestFile(requestFile4)
        }

        @Test
        fun `get all submission request files`() {
            val files = testInstance.getSubmissionRequestFiles("S-BSST1", 1, 0).toList()
            assertThat(files).hasSize(4)
            assertThat(files[0].first).isEqualTo(extFile1)
            assertThat(files[0].second).isEqualTo(1)
            assertThat(files[1].first).isEqualTo(extFile2)
            assertThat(files[1].second).isEqualTo(2)
            assertThat(files[2].first).isEqualTo(extFile3)
            assertThat(files[2].second).isEqualTo(3)
            assertThat(files[3].first).isEqualTo(extFile4)
            assertThat(files[3].second).isEqualTo(4)
        }

        @Test
        fun `get submission request files starting at N`() {
            val files = testInstance.getSubmissionRequestFiles("S-BSST1", 1, 2).toList()
            assertThat(files).hasSize(2)
            assertThat(files[0].first).isEqualTo(extFile3)
            assertThat(files[0].second).isEqualTo(3)
            assertThat(files[1].first).isEqualTo(extFile4)
            assertThat(files[1].second).isEqualTo(4)
        }

        @Test
        fun `get file list specific submission request files`() {
            val files = testInstance.getRequestFileListFiles("S-BSST1", 1, "file-list").toList()
            assertThat(files).hasSize(2)
            assertThat(files[0]).isEqualTo(extFile2)
            assertThat(files[1]).isEqualTo(extFile3)
        }
    }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
            .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
