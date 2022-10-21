package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.jsonObj
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

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionRequestMongoPersistenceServiceTest(
    @MockK private val serializationService: ExtSerializationService,
    @Autowired private val requestRepository: SubmissionRequestDocDataRepository,
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
    fun `update request index`() {
        requestRepository.save(testRequest())

        testInstance.updateRequestIndex("S-BSST0", 1, 23)

        val request = requestRepository.getByAccNoAndVersion("S-BSST0", 1)
        assertThat(request.currentIndex).isEqualTo(23)
        assertThat(request.modificationTime).isEqualTo(testInstant)
    }

    @Test
    fun `update total files`() {
        requestRepository.save(testRequest())

        testInstance.updateRequestTotalFiles("S-BSST0", 1, 50)

        val request = requestRepository.getByAccNoAndVersion("S-BSST0", 1)
        assertThat(request.totalFiles).isEqualTo(50)
        assertThat(request.modificationTime).isEqualTo(testInstant)
    }

    private fun testRequest() = DocSubmissionRequest(
        id = ObjectId(),
        accNo = "S-BSST0",
        version = 1,
        status = CLEANED,
        draftKey = null,
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
