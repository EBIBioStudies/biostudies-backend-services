package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.jsonObj
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.*
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
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionRequestDocDataRepositoryTest(
    @Autowired val testInstance: SubmissionRequestDocDataRepository,
) {

    @BeforeEach
    fun beforeEach() {
        testInstance.deleteAll()
    }

    @Test
    fun saveRequestWhenNew() {
        val request = DocSubmissionRequest(
            id = ObjectId(),
            accNo = "abc-123",
            version = 1,
            status = RequestStatus.CLEANED,
            draftKey = "temp-123",
            notifyTo = "user@test.org",
            submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
            totalFiles = 5,
            currentIndex = 6,
            modificationTime = Instant.now()
        )

        val (_, created) = testInstance.saveRequest(request)

        assertThat(created).isTrue()
        val newRequest = testInstance.getById(request.id)
        assertThat(newRequest.accNo).isEqualTo(request.accNo)
        assertThat(newRequest.version).isEqualTo(request.version)
        assertThat(newRequest.status).isEqualTo(request.status)
        assertThat(newRequest.draftKey).isEqualTo(request.draftKey)
        assertThat(newRequest.notifyTo).isEqualTo(request.notifyTo)
        assertThat(newRequest.submission).isEqualTo(request.submission)
        assertThat(newRequest.totalFiles).isEqualTo(request.totalFiles)
        assertThat(newRequest.currentIndex).isEqualTo(request.currentIndex)
        assertThat(newRequest.modificationTime).isCloseTo(request.modificationTime, within(100, ChronoUnit.MILLIS))
    }

    @Test
    fun saveRequestWhenExists() {
        val (existing, _) = testInstance.saveRequest(
            DocSubmissionRequest(
                id = ObjectId(),
                accNo = "abc-123",
                version = 1,
                status = RequestStatus.CLEANED,
                draftKey = "temp-123",
                notifyTo = "user@test.org",
                submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                totalFiles = 5,
                currentIndex = 6,
                modificationTime = Instant.now()
            )
        )

        val newRequest = DocSubmissionRequest(
            id = ObjectId(),
            accNo = "abc-123",
            version = 2,
            status = RequestStatus.REQUESTED,
            draftKey = "temp-987-b",
            notifyTo = "user-b@test.org",
            submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0-b" }.toString()),
            totalFiles = 51,
            currentIndex = 61,
            modificationTime = Instant.now().plusSeconds(10)
        )
        val (_, created) = testInstance.saveRequest(newRequest)

        assertThat(created).isFalse()

        val submissions = testInstance.findByAccNo(newRequest.accNo)
        assertThat(submissions).hasSize(1)

        val request = submissions.first()
        assertThat(request.accNo).isEqualTo(existing.accNo)
        assertThat(request.version).isEqualTo(existing.version)
        assertThat(request.status).isEqualTo(existing.status)
        assertThat(request.draftKey).isEqualTo(existing.draftKey)
        assertThat(request.notifyTo).isEqualTo(existing.notifyTo)
        assertThat(request.submission).isEqualTo(existing.submission)
        assertThat(request.totalFiles).isEqualTo(existing.totalFiles)
        assertThat(request.currentIndex).isEqualTo(existing.currentIndex)
        assertThat(request.modificationTime).isCloseTo(existing.modificationTime, within(100, ChronoUnit.MILLIS))
    }

    private companion object {
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
