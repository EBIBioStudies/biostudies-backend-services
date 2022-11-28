package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.createNfsFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
import java.time.Instant

@ExtendWith(MockKExtension::class, SpringExtension::class, TemporaryFolderExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionRequestMongoPersistenceServiceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val serializationService: ExtSerializationService,
    @Autowired private val requestRepository: SubmissionRequestDocDataRepository,
    @Autowired private val requestFilesRepository: SubmissionRequestFilesRepository,
) {
    private val testInstant = Instant.ofEpochMilli(1664981331)
    private val testInstance = SubmissionRequestMongoPersistenceService(serializationService, requestRepository)

    @BeforeEach
    fun beforeEach() {
        mockkStatic(Instant::class)
        every { Instant.now() } returns testInstant
    }

    @AfterEach
    fun afterEach() {
        requestRepository.deleteAll()
        unmockkStatic(Instant::class)
    }

    @Test
    fun `update requestFile`() {
        val extFile = createNfsFile("requested.txt", "Files/requested.txt", tempFolder.createFile("requested.txt"))
        val requestFile = SubmissionRequestFile("S-BSST0", 1, index = 2, "requested.txt", extFile)

        requestRepository.upsertSubmissionRequestFile(requestFile)
        requestRepository.save(testRequest())

        testInstance.updateRequestFile(requestFile.copy(file = extFile.copy(md5 = "changedMd5")))

        val request = requestRepository.getByAccNoAndVersion("S-BSST0", 1)
        assertThat(request.modificationTime).isEqualTo(testInstant)
        assertThat(request.currentIndex).isEqualTo(2)

        val savedFile = requestFilesRepository.getByPathAndAccNoAndVersion(requestFile.path, "S-BSST0", 1)
        assertThat(savedFile.file.get("md5")).isEqualTo("changedMd5")
    }

    private fun testRequest() = DocSubmissionRequest(
        id = ObjectId(),
        accNo = "S-BSST0",
        version = 1,
        status = CLEANED,
        draftKey = null,
        notifyTo = "user@test.org",
        submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
        totalFiles = 5,
        currentIndex = 0,
        modificationTime = Instant.ofEpochMilli(1664981300)
    )

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
