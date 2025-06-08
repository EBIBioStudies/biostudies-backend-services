package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestFilesDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.test.clean
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.Duration.ofSeconds

@ExtendWith(MockKExtension::class, SpringExtension::class, TemporaryFolderExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionRequestFilesMongoPersistenceServiceTest(
    private val tempFolder: TemporaryFolder,
    @Autowired private val extSerializationService: ExtSerializationService,
    @Autowired private val requestRepository: SubmissionRequestDocDataRepository,
    @Autowired private val requestFilesRepository: SubmissionRequestFilesDocDataRepository,
) {
    private val testInstance =
        SubmissionRequestFilesMongoPersistenceService(
            extSerializationService,
            requestRepository,
            requestFilesRepository,
        )

    @AfterEach
    fun afterEach() =
        runBlocking {
            tempFolder.clean()
            requestRepository.deleteAll()
        }

    @Nested
    inner class Registration {
        @Test
        fun `register request file`() =
            runTest {
                val extFile =
                    createNfsFile("requested.txt", "Files/requested.txt", tempFolder.createFile("requested.txt"))
                val requestFile = SubmissionRequestFile("S-BSST0", 1, "requested.txt", extFile, INDEXED)

                testInstance.saveSubmissionRequestFile(requestFile)

                val saved = testInstance.getSubmissionRequestFile("S-BSST0", 1, "requested.txt")
                assertThat(saved).isEqualTo(requestFile)
            }

        @Test
        fun `update request file`() =
            runTest {
                val first = createNfsFile("first.txt", "Files/first.txt", tempFolder.createFile("first.txt"))
                val second = createNfsFile("second.txt", "Files/second.txt", tempFolder.createFile("second.txt"))

                val requestFile = SubmissionRequestFile("S-BSST0", 2, "updated.txt", first, INDEXED)
                testInstance.saveSubmissionRequestFile(requestFile)

                val updatedFile = SubmissionRequestFile("S-BSST0", 2, "updated.txt", second, INDEXED)
                testInstance.saveSubmissionRequestFile(updatedFile)

                val updated = testInstance.getSubmissionRequestFile("S-BSST0", 2, "updated.txt")
                assertThat(updated).isEqualTo(updatedFile)
            }
    }

    @Nested
    inner class Query {
        private val extFile1 = createNfsFile("file1.txt", "Files/file1.txt", tempFolder.createFile("file1.txt"))
        private val extFile2 = createNfsFile("file2.txt", "Files/file2.txt", tempFolder.createFile("file2.txt"))
        private val extFile3 = createNfsFile("file3.txt", "Files/file3.txt", tempFolder.createFile("file3.txt"))
        private val extFile4 = createNfsFile("file4.txt", "Files/file4.txt", tempFolder.createFile("file4.txt"))

        @BeforeEach
        fun beforeEach() =
            runBlocking {
                val requestFile1 = SubmissionRequestFile("S-BSST1", 1, "file1.txt", extFile1, INDEXED)
                val requestFile2 = SubmissionRequestFile("S-BSST1", 1, "file2.txt", extFile2, LOADED)
                val requestFile3 = SubmissionRequestFile("S-BSST1", 1, "file3.txt", extFile3, COPIED)
                val requestFile4 = SubmissionRequestFile("S-BSST1", 1, "file4.txt", extFile4, COPIED)

                testInstance.saveSubmissionRequestFile(requestFile1)
                testInstance.saveSubmissionRequestFile(requestFile2)
                testInstance.saveSubmissionRequestFile(requestFile3)
                testInstance.saveSubmissionRequestFile(requestFile4)
            }

        @Test
        fun `get all submission request files`() =
            runTest {
                val files = testInstance.getSubmissionRequestFiles("S-BSST1", 1, 0).toList()
                assertThat(files).hasSize(4)
                assertThat(files[0].file).isEqualTo(extFile1)
                assertThat(files[1].file).isEqualTo(extFile2)
                assertThat(files[2].file).isEqualTo(extFile3)
                assertThat(files[3].file).isEqualTo(extFile4)
            }

        @Test
        fun `get submission request files starting at N`() =
            runTest {
                val files = testInstance.getSubmissionRequestFiles("S-BSST1", 1, 2).toList()
                assertThat(files).hasSize(2)
                assertThat(files[0].file).isEqualTo(extFile3)
                assertThat(files[1].file).isEqualTo(extFile4)
            }

        @Test
        fun `get submission request files by status`() =
            runTest {
                val files = testInstance.getSubmissionRequestFiles("S-BSST1", 1, COPIED).toList()
                assertThat(files).hasSize(2)
                assertThat(files[0].file).isEqualTo(extFile3)
                assertThat(files[1].file).isEqualTo(extFile4)
            }
    }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
