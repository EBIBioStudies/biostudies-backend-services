package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.jsonObj
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
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

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionMongoPersistenceServiceTest(
    @MockK private val serializationService: ExtSerializationService,
    @MockK private val submissionRepository: ExtSubmissionRepository,
    @MockK private val subDataRepository: SubmissionDocDataRepository,
    @Autowired private val requestRepository: SubmissionRequestDocDataRepository,
) {
    private val testInstance =
        SubmissionMongoPersistenceService(
            subDataRepository,
            requestRepository,
            serializationService,
            submissionRepository,
        )

    @AfterEach
    fun afterEach() {
        requestRepository.deleteAll()
    }

    @Test
    fun `update request index`() {
        requestRepository.save(testRequest())

        testInstance.updateRequestIndex("S-BSST0", 1, 23)

        assertThat(requestRepository.getByAccNoAndVersion("S-BSST0", 1).currentIndex).isEqualTo(23)
    }

    @Test
    fun `update total file`() {
        requestRepository.save(testRequest())

        testInstance.updateRequestTotalFiles("S-BSST0", 1, 50)

        assertThat(requestRepository.getByAccNoAndVersion("S-BSST0", 1).totalFiles).isEqualTo(50)
    }

    private fun testRequest() = DocSubmissionRequest(
        id = ObjectId(),
        accNo = "S-BSST0",
        version = 1,
        status = CLEANED,
        draftKey = null,
        submission = BasicDBObject.parse(jsonObj { "submission" }.toString()),
        totalFiles = 5,
        currentIndex = 0,
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
